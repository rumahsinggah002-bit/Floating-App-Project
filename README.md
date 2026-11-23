# GOOGLE FLOAT

Aplikasi Android native dengan floating overlay sungguhan (WindowManager + SYSTEM_ALERT_WINDOW) dan browser WebView.

## Build APK
- Buka project ini dengan Android Studio (JDK 17, Android SDK 35), lalu pilih **Build > Build APK(s)**.
- Atau unggah isi folder project ke GitHub. Workflow `.github/workflows/build-apk.yml` akan membangun APK ketika push ke branch `main`. Unduh artifact `GOOGLE-FLOAT-debug-apk` dari tab Actions.
- Jika memakai Android Studio, buat/unduh Gradle wrapper bila belum tersedia, atau jalankan `gradle assembleDebug` dengan Gradle terpasang.

## Penggunaan
1. Instal APK debug di Android dan izinkan instalasi dari sumber yang dipilih jika diminta.
2. Buka GOOGLE FLOAT.
3. Tekan **Aktifkan Floating Browser**.
4. Aktifkan izin **Tampil di atas aplikasi lain** pada halaman pengaturan Android.
5. Tombol G akan muncul. Seret untuk memindahkan; ketuk untuk membuka/menutup browser.
6. Gunakan kolom alamat untuk pencarian atau URL. Tombol panah, refresh, home, dan bintang tersedia di toolbar.

## Catatan
- Ini adalah project Android native, bukan PWA. Overlay memakai `TYPE_APPLICATION_OVERLAY`.
- Android 8+ memakai foreground service dengan notifikasi agar layanan lebih stabil. Sistem Android tetap dapat menghentikan layanan pada kondisi tertentu.
- Riwayat URL disimpan lokal (maksimum 30 entri). Tombol bintang menyimpan URL terakhir sebagai favorit di SharedPreferences.
- Tombol resize mengubah ukuran jendela antara ukuran compact dan besar. Tombol minimize/close menutup panel, sedangkan tombol G tetap ada.
- Perilaku izin notifikasi dan foreground service dapat berbeda menurut versi Android/vendor. Pada Android 13+, izinkan notifikasi jika diminta.
- Pastikan penggunaan situs mematuhi ketentuan situs tersebut. Beberapa situs mungkin membatasi tampilan di WebView.
