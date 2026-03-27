# Growl

Spring Boot service that loads bundled CSVs (offers, ad opportunities, chat data) and exposes REST APIs. Ad serving logic lives in `DefaultAdServingDecisionEngine`.

---

## API endpoints

Base URL (default): `http://localhost:8080`

| Method | Path | Query params | Description |
|--------|------|----------------|-------------|
| GET | `/hello` | — | Health-style ping; returns plain text `Hello`. |
| GET | `/offers` | — | All rows from `offers.csv` as JSON (`Offer[]`). |
| GET | `/ad-opportunities` | — | All rows from ad opportunities CSV as JSON (`AdOpportunity[]`). |
| GET | `/chat-data` | — | All rows from chat-data CSV as JSON (`ChatDataRow[]`). |
| GET | `/ad-opportunities/decisions` | `ad_load` (optional, 0–100) | Run the ad decision engine on **every** loaded ad opportunity; returns `AdServingDecision[]` in CSV order. |
| GET | `/ad-opportunities/{id}/decision` | `ad_load` (optional, 0–100) | Single `AdServingDecision` for the opportunity with that `id`. **404** if id unknown. |
| GET | `/visitors/{visitorId}/chats/{chatId}` | — | Full transcript for that visitor + chat id (`ChatDataRow[]`, sorted by `created_at`). Empty array if none. **400** if path ids blank. |
| GET | `/visitors/{visitorId}/chats/{chatId}/potential-ad-opportunities` | — | `PotentialAdOpportunityReport`: assistant vs recorded counts, `missingSlots`, methodology note. **400** if ids blank. |
| GET | `/visitors/{visitorId}/chats/{chatId}/potential-missing-ad-opportunities` | — | JSON array of `{ visitor_id, session_id, chat_id, assistant_message_count }` for gaps. **400** if ids blank. |
| GET | `/visitors/{visitorId}/chats/{chatId}/potential-missing-ad-opportunities.csv` | — | Same as above as **`text/csv`** with header row. |

**`ad_load`:** optional on both decision routes. Clamped to [0, 100]; omitted means 100. Values below 100 randomly suppress some eligible requests (`AD_LOAD_SUPPRESSED`); see [Ad decision flow](#ad-decision-flow).

**Example URLs** (replace ids as needed):

- `http://localhost:8080/hello`
- `http://localhost:8080/offers`
- `http://localhost:8080/ad-opportunities`
- `http://localhost:8080/chat-data`
- `http://localhost:8080/ad-opportunities/decisions`
- `http://localhost:8080/ad-opportunities/decisions?ad_load=50`
- `http://localhost:8080/ad-opportunities/89a43a59-6144-4016-87da-c76a0581b710/decision`
- `http://localhost:8080/visitors/36d4e150-960d-4f0c-bc20-5680c84db9ec/chats/10`
- `http://localhost:8080/visitors/36d4e150-960d-4f0c-bc20-5680c84db9ec/chats/1/potential-ad-opportunities`
- `http://localhost:8080/visitors/36d4e150-960d-4f0c-bc20-5680c84db9ec/chats/1/potential-missing-ad-opportunities`
- `http://localhost:8080/visitors/36d4e150-960d-4f0c-bc20-5680c84db9ec/chats/1/potential-missing-ad-opportunities.csv`

---

## Ad decision flow

### Summary

1. **Gatekeeping:** Not payout-eligible → no ad. `opportunity_index` &gt; 1 → no ad.
2. **Throttle:** Optional **`ad_load`** (0–100%): random skip → no ad (`AD_LOAD_SUPPRESSED`).
3. **Context:** Load **chat transcript** via `visitor_id` + `chat_id` from the opportunity / `slot_data`.
4. **No chat text:** Pick best offer by **revenue score** among **geo** (then **global**) candidates, or no ad if none fit geo/global.
5. **With chat text:** Score **lexical relevance** vs transcript; if nothing scores ≥ **2.0**, no ad. Else pick the offer that best balances **relevance × 18 + revenue**.

**Output:** `AdServingDecision` — `opportunityId`, `ad_slot_id`, `serve`, `offer`, `reasonCode`, optional `chatAnalysis`.

---

Each call takes an **ad opportunity** (a row from `ad-opportunities.csv`) and an optional **`ad_load`** (0–100, default 100). The engine returns an **`AdServingDecision`** with `opportunityId`, `ad_slot_id` (from `slot_id`), `serve`, `offer`, `reasonCode`, and optional `chatAnalysis`.

Steps are evaluated **in order**; the first matching stop returns.

### 1. Payout eligibility

If `payout_eligible` is not treated as true (`true` / `1` / `yes`, case-insensitive) → **do not serve**, `reasonCode`: **`NOT_PAYOUT_ELIGIBLE`**.

### 2. Opportunity index cap

`opportunity_index` is read from `slot_data` JSON. If it is **greater than 1** → **do not serve**, **`OPPORTUNITY_INDEX_CAP`**.

### 3. Ad load (throttle)

`ad_load` is clamped to **[0, 100]**.

- If **0**, or a uniform random draw in **[0, 100)** is **≥ ad_load** → **do not serve**, **`AD_LOAD_SUPPRESSED`**.
- Otherwise the request continues. In expectation, about **`ad_load` percent** of opportunities that reach this step are not suppressed.

### 4. Chat transcript

- **`visitor_id`** comes from the opportunity row.
- **`chat_id`** comes from `slot_data` JSON.
- Messages are loaded from chat-data CSV for that pair, ordered by **`created_at`**.
- A **text corpus** is built from message text; **user** lines are duplicated so user intent is weighted slightly more.

### 5a. No usable chat text (blank corpus)

Used when there is no transcript text to analyze (missing keys, no rows, or only empty messages).

- **Candidates:** offers whose **name** implies geo targeting that **includes** the opportunity’s **`geo_country_code`**; if none, **global** offers (no such region segment in the name).
- **No candidates** → **do not serve**, **`NO_GEO_OR_GLOBAL_MATCH`** (with `chatAnalysis` when applicable).
- **Otherwise** → **serve** the highest **revenue-style** offer (`OfferValueScorer`):
  - **`SELECTED_BEST_SCORED_OFFER_MISSING_KEYS`** if `visitor_id` or `chat_id` is missing.
  - **`SELECTED_BEST_SCORED_OFFER_NO_CHAT`** if keys exist but there is still no corpus.

### 5b. Usable chat text

- **Candidates:** **geo-targeted ∪ global** offers (deduped by offer id) so globally worded offers can compete on text match.
- **No candidates** → **`NO_GEO_OR_GLOBAL_MATCH`**.
- **Lexical relevance** (`OfferChatRelevance`) is scored for each offer against the corpus (offer name + URL host tokens; short/noisy tokens like “visa” are down-weighted).
- If **max relevance &lt; 2.0** → **do not serve**, **`NO_CHAT_RELEVANT_OFFER`** (includes `chatAnalysis` with `bestRelevanceScore`).
- Among offers with **relevance ≥ 2.0**, pick the one that maximizes **`relevance × 18 + OfferValueScorer.score`**, tie-break by offer id → **serve**, **`SELECTED_CHAT_RELEVANT_OFFER`**.

### Constants (implementation)

| Constant | Value | Role |
|----------|-------|------|
| `MAX_OPPORTUNITY_INDEX_TO_SERVE` | 1 | Drop if `opportunity_index` &gt; 1 |
| `MIN_CHAT_RELEVANCE` | 2.0 | Minimum score to consider chat “on topic” |
| `CHAT_WEIGHT` | 18.0 | How much relevance vs revenue in final pick |

Decision endpoints are listed in [API endpoints](#api-endpoints).

---

## Run

```bash
mvn spring-boot:run
```

Default base URL: `http://localhost:8080` (see `application.properties` for `server.port`).
