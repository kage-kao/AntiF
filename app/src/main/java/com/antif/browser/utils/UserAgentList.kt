package com.antif.browser.utils

import kotlin.random.Random

object UserAgentList {

    private val chromeVersions = listOf(
        "120.0.0.0", "121.0.0.0", "122.0.0.0", "123.0.0.0",
        "124.0.0.0", "125.0.0.0", "126.0.0.0", "127.0.0.0",
        "128.0.0.0", "129.0.0.0", "130.0.0.0", "131.0.0.0"
    )

    private val windowsVersions = listOf(
        "Windows NT 10.0; Win64; x64",
        "Windows NT 10.0; WOW64",
        "Windows NT 11.0; Win64; x64"
    )

    private val macVersions = listOf(
        "Macintosh; Intel Mac OS X 10_15_7",
        "Macintosh; Intel Mac OS X 11_6_0",
        "Macintosh; Intel Mac OS X 12_3_1",
        "Macintosh; Intel Mac OS X 13_4",
        "Macintosh; Intel Mac OS X 14_0"
    )

    private val linuxVersions = listOf(
        "X11; Linux x86_64",
        "X11; Ubuntu; Linux x86_64"
    )

    fun getRandomUserAgent(): String {
        val random = Random(System.nanoTime())
        val chromeVer = chromeVersions.random(random)
        val safariVer = "537.36"

        val os = when (random.nextInt(3)) {
            0 -> windowsVersions.random(random)
            1 -> macVersions.random(random)
            else -> linuxVersions.random(random)
        }

        return "Mozilla/5.0 ($os) AppleWebKit/$safariVer (KHTML, like Gecko) Chrome/$chromeVer Safari/$safariVer"
    }

    fun getWindowsUserAgent(): String {
        val chromeVer = chromeVersions.random()
        val win = windowsVersions.random()
        return "Mozilla/5.0 ($win) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVer Safari/537.36"
    }

    fun getMacUserAgent(): String {
        val chromeVer = chromeVersions.random()
        val mac = macVersions.random()
        return "Mozilla/5.0 ($mac) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVer Safari/537.36"
    }

    fun getLinuxUserAgent(): String {
        val chromeVer = chromeVersions.random()
        val linux = linuxVersions.random()
        return "Mozilla/5.0 ($linux) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVer Safari/537.36"
    }
}
