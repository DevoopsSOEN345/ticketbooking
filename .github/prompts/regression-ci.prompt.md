---
description: Generate enterprise GitHub Actions regression CI for this repository
---
You are a senior Mobile DevOps engineer and CI/CD architect.

Goal:
Create a production-ready regression CI/CD setup for this repository.

Requirements:
- Triggers for relevant workflows:
  - `push` on any branch (`branches: ['**']`)
  - `pull_request` on any branch (`branches: ['**']`)
  - `workflow_dispatch`
- Separate workflows by concern:
  - `.github/workflows/unittest.yml`
  - `.github/workflows/integrationtest.yml`
  - `.github/workflows/end2endtest.yml`
  - `.github/workflows/sonarcloud.yml`
  - `.github/workflows/regression.yml` (orchestrator)
- SonarCloud:
  - use `SONAR_TOKEN` from GitHub Secrets
  - support PR analysis
  - checkout with full history where needed (`fetch-depth: 0`)
  - analyze checked-out source code (not external artifact repositories)
  - ingest JaCoCo XML using `sonar.coverage.jacoco.xmlReportPaths`
- Coverage:
  - do not use Codecov
  - generate JaCoCo XML
  - upload with `actions/upload-artifact@v4`
  - Sonar job must download with `actions/download-artifact@v4`
  - include a step named exactly `Download Coverage Artifact` where appropriate
- Artifact handling:
  - do not use JFrog/Artifactory
  - use only native GitHub Actions artifacts
  - preserve reports even on failure where possible (`if: always()`)
- Quality:
  - modern syntax
  - least-privilege `permissions`
  - `concurrency` to cancel superseded runs per branch
  - Gradle cache/setup where appropriate
  - readable enterprise naming and structure
  - retention-days set for artifacts
- Monorepo/mobile awareness:
  - keep paths easy to customize
  - JaCoCo only for JVM modules; do not fake JS/TS as JaCoCo
  - do not invent unsupported tooling

If repository is Gradle-based:
- use `actions/setup-java`
- use `gradle/actions/setup-gradle`
- run proper test + JaCoCo tasks

If repository is Maven-based:
- use `actions/setup-java`
- use Maven caching if appropriate
- run JaCoCo + Sonar via Maven plugin

Output format (strict):
1) Short architecture summary.
2) Full content of each workflow YAML file.
3) Any `sonar-project.properties` content if needed.
4) Any required Gradle/Maven JaCoCo config.
5) Required GitHub Secrets list.
6) Short explanation of workflow interactions.

Implementation style:
- Provide complete, ready-to-paste files.
- Avoid vague placeholders.
- If a value is project-specific, mark it with `CHANGE_ME`.

