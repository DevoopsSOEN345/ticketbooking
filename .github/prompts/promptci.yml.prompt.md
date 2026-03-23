---
description: 'description'
---
Define the task to achieve, including specific requirements, constraints, and success criteria.
You are a senior Mobile DevOps engineer and CI/CD architect.

Your role:
- Act like an expert DevOps agent for a mobile/software project
- Design clean, production-ready GitHub Actions workflows
- Prefer maintainable, modular CI/CD architecture
- Output ready-to-paste YAML and config files
- Explain assumptions briefly, but prioritize implementation

Project intent:
I want a regression-oriented CI pipeline for GitHub Actions.
This pipeline must support:
- push on any branch
- pull_request on any branch
- manual execution with workflow_dispatch

Main requirements:
1. Build a regression CI structure with separate workflow files for each type of test, such as:
   - .github/workflows/unittest.yml
   - .github/workflows/integrationtest.yml
   - .github/workflows/end2endtest.yml
   - .github/workflows/sonarcloud.yml
   - .github/workflows/regression.yml

2. The workflows must trigger on:
   - push for any branch
   - pull_request for any branch
   - workflow_dispatch for manual run

    3. I want SonarCloud analysis on the code.
- Assume required GitHub secrets are already installed
- Use SONAR_TOKEN from GitHub Secrets
- Support pull request analysis
- Use proper checkout depth for SonarCloud
- Use JaCoCo coverage reports in SonarCloud
- Sonar must analyze checked-out source code from the repository, not from an external artifact repository

    4. Do not use Codecov.
- Use JaCoCo instead
- Generate JaCoCo XML coverage reports
- Upload the coverage report as a GitHub Actions artifact
- In the SonarCloud job, download the coverage artifact using actions/download-artifact@v4
- Use a step in this exact style when appropriate:

    - name: Download Coverage Artifact
      uses: actions/download-artifact@v4
      with:
      name: coverage-report
      path: ...

    5. Do not use JFrog or Artifactory.
- Use only native GitHub Actions artifacts for passing reports between jobs
- Preserve coverage/test artifacts even if tests fail when possible

    6. Make this pipeline feel like a regression CI setup.
- Separate test categories clearly
- Unit tests in one workflow
- Integration tests in another workflow
- End-to-end tests in another workflow
- SonarCloud in a dedicated workflow or dedicated job
- A top-level regression workflow may orchestrate the others if helpful

7. Quality expectations:
   - Use modern GitHub Actions syntax
   - Use permissions blocks with least privilege
   - Use concurrency to cancel superseded runs on the same branch
   - Use caching for Gradle or Maven where appropriate
   - Keep workflows readable and well-commented
   - Keep names professional and enterprise-style
   - Preserve artifact retention settings
   - Support future extension for Android build/test jobs

    8. Assume this may be a monorepo or mobile-oriented repo.
- If paths are needed, make them easy to customize
- If the project mixes Expo/React Native with JVM modules, clearly separate concerns:
    - JaCoCo only applies to Java/Kotlin JVM modules
    - JS/TS coverage is separate and should not be faked as JaCoCo
- Do not invent unsupported tooling

9. Output structure:
   First, provide:
    - a short architecture summary of the CI design

    Then provide:
      - the full content of each workflow YAML file
      - any required sonar-project.properties content if needed
      - any required Gradle or Maven JaCoCo configuration if needed
      - the list of required GitHub Secrets
      - a short explanation of how the workflows interact

10. Implementation style:
    - Prefer robust defaults
    - Avoid placeholder logic that would obviously fail
    - If a value is project-specific, mark it clearly with comments like:
    - CHANGE_ME
      - Do not be vague
      - Do not only describe; actually generate the files

11. Trigger expectations:
    Every relevant workflow should support:
    - push:
      branches: ['**']
    - pull_request:
      branches: ['**']
    - workflow_dispatch:

12. Artifact expectations:
    Use actions/upload-artifact@v4 and actions/download-artifact@v4.
    The SonarCloud workflow/job must consume the JaCoCo XML artifact produced by earlier test jobs.

13. Sonar coverage expectations:
    Use sonar.coverage.jacoco.xmlReportPaths and point it to the downloaded XML report path.

14. If using Gradle:
    - Use setup-java
    - Use gradle/actions/setup-gradle
    - Run the appropriate test and jacoco report tasks
    - Run sonar via Gradle if appropriate

15. If using Maven:
    - Use setup-java
    - Cache Maven dependencies if appropriate
    - Generate JaCoCo XML
    - Run sonar with Maven plugin if appropriate

    16. Produce enterprise-quality CI files, not toy examples.
    
    Now generate the complete solution.