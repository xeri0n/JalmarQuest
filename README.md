# JalmarQuest

## AI Director Configuration

The AI Director now supports both sandbox fixtures and live Gemini responses. Configure the service at runtime via environment variables:

| Variable | Description |
| --- | --- |
| `AI_DIRECTOR_MODE` | `sandbox` (default) serves responses from fixtures, `live` enables Gemini integration. |
| `AI_DIRECTOR_SANDBOX_FIXTURES` | Optional path to a JSON fixture file; defaults to the bundled `aidirector/fixtures/sandbox_prompt.json`. |
| `GEMINI_API_KEY` | Required in `live` mode. Google Generative Language API key. |
| `GEMINI_MODEL` | Optional model name (default `gemini-1.5-pro`). |
| `GEMINI_BASE_URL` | Optional base URL override (default `https://generativelanguage.googleapis.com`). |
| `GEMINI_TIMEOUT_MILLIS` | Optional request timeout in milliseconds (default `15000`). |
| `AI_DIRECTOR_RATE_LIMIT_MAX_REQUESTS` | Optional live rate-limit window size. |
| `AI_DIRECTOR_RATE_LIMIT_INTERVAL_MILLIS` | Optional live rate-limit interval in milliseconds. |

At startup, call `AiDirectorFactory.createService(AiDirectorConfig.fromEnvironment())` (or `createChapterEventProvider`) to obtain a fully wired `AiDirectorService` that respects these settings.

### Quick Bootstrap

For backend entry points, use `AiDirectorDeployment.bootstrap()` to create the service, a `ChapterEventProvider`, and a Koin context in one call:

```kotlin
fun main() {
	val handles = AiDirectorDeployment.bootstrap()
	val service = handles.service
	// use service here
}
```