# Android Z-Wave #
Android Z-Wave adalah sebuah aplikasi Android yang dapat mengendalikan perangkat Z-Wave secara langsung melalui Z-Wave USB Controller. Aplikasi ini mengimplementasi [Open Z-Wave](https://code.google.com/p/open-zwave/) yang telah diporting ke Java sehingga dapat digunakan untuk membuat aplikasi Android. Aplikasi ini hanyalah hobi untuk mencoba melakukan porting Open Z-Wave ke Java.

**Kode sumber telah tersedia pada SVN Proyek ini**

<img src='https://android-zwave.googlecode.com/svn/screenshoot/MainScreenshot.jpg' />

## Perangkat ##
  * Z-Wave USB Controller : Aeon Labs Z-Wave Z-Stick Series 2 USB Dongle (CP2102).
  * Perangkat Android yang mendukung [USB Host API](http://developer.android.com/guide/topics/connectivity/usb/host.html)

## Dependensi ##
  * USB Serial for Android : https://code.google.com/p/usb-serial-for-android/ (Jika CP2102 tidak berjalan dengan benar, gunakan kode [ini](http://code.google.com/p/android-zwave/source/browse/screenshoot/Cp2102SerialDriver.java))

## Limitasi ##
  * Belum semua source di porting ke Java (masalah waktu dan kesibukan)
  * Tidak semua yang didukung oleh Open Z-Wave dapat didukung oleh Android Z-Wave
  * GUI Android Z-Wave hanya dapat mengendalikan: Primary Controller, Appliance Module, dan Dimmer.

## Keterangan ##
  * Aplikasi ini menggunakan [Open Z-Wave](https://code.google.com/p/open-zwave/) yang telah diporting ke Java. Hak cipta dari seluruh/sebagian komponen Open Z-Wave yang digunakan pada aplikasi ini adalah milik pengembang Open Z-Wave.
  * Pengembang Android Z-Wave adalah seorang pemula dan aplikasi masih dalam tahap experimental, kesalahan mungkin saja terjadi pada setiap source code yang ada.

## Proyek Terkait ##
  * Open Z-Wave : https://code.google.com/p/open-zwave/

## Diskusi ##
  * Forum Diskusi untuk Android Z-Wave : https://groups.google.com/forum/#!topic/openzwave/TA6J6mYRBSQ

## For International Users ##
Please use [Google Translate](http://translate.google.com/) to translate this page, thanks you.