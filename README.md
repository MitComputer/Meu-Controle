# Meu Controle — Beta Android

## Upload pelo celular
Este pacote foi achatado de propósito para que o GitHub pelo celular aceite os arquivos sem você precisar selecionar pastas.

Envie estes arquivos para a raiz do repositório:
- AndroidManifest.xml
- MainActivity.kt
- build.gradle.kts
- gradle.properties
- settings.gradle.kts
- README.md

Depois crie manualmente no GitHub o arquivo:
`.github/workflows/build-apk.yml`

Use o conteúdo abaixo:

```yaml
name: Build APK
on:
  workflow_dispatch:
  push:
    branches: [ "main", "master" ]
jobs:
  build:
    runs-on: ubuntu-latest
    permissions:
      contents: read
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "17"
      - uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: "8.10"
      - uses: android-actions/setup-android@v3
      - run: sdkmanager "platforms;android-35" "build-tools;35.0.0"
      - run: gradle assembleDebug --stacktrace
      - uses: actions/upload-artifact@v4
        with:
          name: MeuControle-beta-debug
          path: build/outputs/apk/debug/app-debug.apk
```

## Importante
Esta beta usa dados em memória. A persistência com Room, contas pendentes, calendário, filtros, transferências, backup e PDF serão implementados depois.
