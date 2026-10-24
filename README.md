# RootCrack

Root manager untuk Android yang nge-patch `boot.img` / `init_boot.img` langsung
di level kernel — arsitektur ala APatch/KernelPatch, ditulis dari nol:

- **app/** — Android app (Kotlin + Jetpack Compose): Install (patch/unpatch),
  Module manager (+ WebUI per-module), KPM (kernel module) loader, SuperUser
  access control.
- **daemon/** (`rfd`, Rust) — daemon userspace yang jalan sebagai root: staged
  module install, autoload KPM saat boot, kelola profil su, patch sepolicy.
  Bicara ke kernel lewat satu syscall `supercall` (lihat
  `app/src/main/cpp/kp_uapi.h`).
- **app/src/main/cpp/** — JNI tipis yang manggil syscall `supercall` yang sama.

## Soal `kpimg` / `kptools`

RootCrack **tidak** menulis ulang mesin yang benar-benar menyuntik kode ke
kernel image (`kpimg`) dan tool pemaketnya (`kptools`). Itu didownload saat
build dari rilis resmi project
[KernelPatch](https://github.com/bmax121/KernelPatch) (lihat
`app/build.gradle.kts` task `downloadKpimg` / `downloadKptools`), persis
seperti yang dilakukan APatch. Versi yang ditarget ada di
`app/src/main/cpp/kp_abi_version`.

Kenapa begitu: nulis ulang bagian itu dari nol butuh riset ARM64
kernel-symbol-resolving & syscall-hooking yang sangat dalam, dan kalau ada
bug bisa bikin HP bootloop permanen. Pakai binary resmi yang udah diuji
komunitas jauh lebih aman buat kamu maupun orang lain yang install hasil
build ini.

## Build lokal

Butuh: JDK 17, Android SDK (API 35), NDK r27+, Rust + `cargo-ndk`
(`cargo install cargo-ndk`), target `aarch64-linux-android`.

```bash
# generate gradle wrapper sekali (butuh Gradle terpasang di sistem)
gradle wrapper --gradle-version 8.11

./gradlew :app:assembleDebug
```

APK ada di `app/build/outputs/apk/debug/`.

## Build via GitHub Actions

Workflow `.github/workflows/build.yml` otomatis build tiap push ke `main`
atau manual lewat tab Actions → Run workflow. Hasil APK ada di tab
Artifacts pada run tersebut.

Buat release APK yang ditandatangani, isi secrets repo ini:
`KEYSTORE_FILE` (base64 keystore), `KEYSTORE_PASSWORD`, `KEY_ALIAS`,
`KEY_PASSWORD`.

## Peringatan

Patch boot image ngubah kernel HP kamu di level paling dalam. **Selalu**
backup `boot.img`/`init_boot.img` original sebelum patch (script
`boot_patch.sh` otomatis bikin `ori.img`), dan pastikan kamu punya cara
balik ke stock (fastboot, EDL, dsb) kalau device gagal boot. Dipakai atas
risiko sendiri.

## Lisensi

GPL-3.0-or-later. Lihat `LICENSE`.
