# 🎨 DyeTube

DyeTube is a YouTube client for Android with no ads, lots of animations and a look you can dye to your taste.
It is a fork of [NewPipe](https://github.com/TeamNewPipe/NewPipe): no Google services, no account needed, nothing tracked.

## Features

**From NewPipe**
- No ads, no Google Play Services
- Background and popup (picture-in-picture) playback
- Download videos and audio
- Subscriptions and playlists stored on the phone (import from YouTube is supported)

**DyeTube**
- 8 app colors: Dye (the logo gradient), Violet, Sunset, Ocean, Mint, Ruby, Gold, Graphite
- Gradient top bar
- Thumbnail corners from square to very rounded
- Launch animation: drops of dye fly together and the play triangle springs out
- Colored rings where you tap
- Screens rise and spring, lists flow in, cards spring when pressed
- All animations can be turned off in Settings → DyeTube

## Download

APK files are on the [Releases](https://github.com/cleveroze/DyeTube/releases) page. Android 6.0 or newer.

## Building

```
./gradlew :app:assembleRelease
```

Release builds are signed with the key from `~/DyeTube-Keys/signing.properties` when it exists.

## License

GPL-3.0-or-later, like NewPipe. DyeTube is not affiliated with YouTube, Google or the NewPipe team.
Most of the code is NewPipe's; thanks to the NewPipe contributors.
