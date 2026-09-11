# Secret Exposure Runbook

The historical Firebase service-account file and Food Safety API key must be treated as exposed.

1. Revoke the Firebase service-account key in Google Cloud IAM. The application no longer uses Firebase, so disable or delete that service account when no other workload depends on it.
2. Revoke the Food Safety API key and create a replacement. Store its value only in the deployment platform's secret manager.
3. Confirm the replacement is deployed and audit provider access logs before rewriting Git history.
4. Create a fresh mirror clone and run `git filter-repo --sensitive-data-removal --invert-paths --path service-account.json --path openapi.js`.
5. Force-push all rewritten refs, notify collaborators to reclone, clear CI caches, and enable GitHub secret scanning.

Do not run the history rewrite until the affected credentials have been revoked or rotated.
