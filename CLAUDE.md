# DyeTube — CLAUDE.md

DyeTube — YouTube-клиент для Android, форк NewPipe v0.29.1 (2026-11-04). Без рекламы, с анимациями и
кастомизацией. Владелец тот же, что у OtherGram (cleveroze). Отвечать по-русски, просто.

- Репозиторий: https://github.com/cleveroze/DyeTube (ветка `main`, история начата заново одним коммитом).
- Отправлять код на GitHub и делать релизы — только по просьбе владельца.
- Лицензия GPL-3.0: код открытый, NewPipe указан в README.

## Что поменяно относительно NewPipe
- Пакет `app.dyetube.android`, версия `DYETUBE_VERSION_NAME`/`DYETUBE_VERSION_CODE` в
  `buildSrc/src/main/kotlin/ProjectConfig.kt` (Java-пакет внутри остался `org.schabi.newpipe`).
- Название `DyeTube` (`resValue` в `app/build.gradle.kts`, «NewPipe» → «DyeTube» в строках, кроме
  пожертвований, сайта, политики конфиденциальности и сообщений проекта NewPipe).
- Иконки: `Tools/dyetube/generate-icons.ps1` из `icon-source.png` и `logo-mark.png`. Заставка тёмная
  (`dye_splash_background`) с треугольником.
- Цвета бренда в `values/dye_colors.xml`, акценты — оверлеи `values/dye_styles.xml`.
- Свой код — Kotlin в `org.schabi.newpipe.dye` (Java проверяет checkstyle: final-параметры, строки ≤ 100):
  `Dye` (настройки, акцент, градиентная панель, скругление превью, анимации переходов), `DyeMotion`
  (круги при нажатии, появление списков, пружина карточек), `DyeLaunchAnimation`, `DyeSettingsFragment`.
- Хуки (Java, полные имена классов, комментарий `// DyeTube:`): `ThemeHelper.setTheme`,
  `MainActivity.onCreate`/`dispatchTouchEvent`, `BaseListFragment.initViews`, `InfoItemHolder`,
  `NavigationHelper.defaultTransaction`, `SettingsActivity.showSettingsFragment`, `SettingsResourceRegistry`.
- Обновления NewPipe не приходят: проверка обновлений сравнивает подпись с ключом NewPipe.

## Сборка
- Git Bash: `cd /c/Users/FunkyUser/DyeTube && export JAVA_HOME="/c/Users/FunkyUser/.jdks/jbr-21.0.11" &&
  ./gradlew :app:assembleRelease` (первая сборка ~30 мин, дальше 12–15).
- APK: `app/build/outputs/apk/release/app-release.apk`.
- `local.properties`: `sdk.dir=C:/Users/FunkyUser/AppData/Local/Android/Sdk` (прямые слэши!).
- Ключ: `C:\Users\FunkyUser\DyeTube-Keys` (`dyetube-release.jks`, пароль в `README-KEY.txt`,
  `signing.properties` читает сборка). Никогда не коммитить.

## Идеи на потом
- SponsorBlock (пропуск спонсорских вставок, API sponsor.ajay.app без ключа).
- Проверка обновлений через релизы GitHub DyeTube.
