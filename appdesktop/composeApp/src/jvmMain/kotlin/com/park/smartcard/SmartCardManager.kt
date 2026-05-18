//package com.park.smartcard
//
//import javax.smartcardio.CommandAPDU
//import javax.smartcardio.TerminalFactory
//
//class SmartCardManager {
//    private val terminals = runCatching {
//        TerminalFactory.getDefault().terminals()
//    }.getOrNull()
//
//    fun listReaders(): List<String> {
//        return runCatching {
//            terminals?.list()?.map { it.name }.orEmpty()
//        }.getOrDefault(emptyList())
//    }
//
//    fun readCardUID(readerName: String? = null): String? {
//        return runCatching {
//            val terminal = if (readerName.isNullOrBlank()) {
//                terminals?.list()?.firstOrNull()
//            } else {
//                terminals?.list()?.firstOrNull { it.name == readerName }
//            } ?: return null
//
//            if (!terminal.isCardPresent) return null
//
//            val card = terminal.connect("*")
//            try {
//                val response = card.basicChannel
//                    .transmit(CommandAPDU(byteArrayOf(0xFF.toByte(), 0xCA.toByte(), 0x00, 0x00, 0x00)))
//                    .bytes
//
//                if (response.size < 2) return null
//                val sw = ((response[response.size - 2].toInt() and 0xFF) shl 8) or
//                    (response[response.size - 1].toInt() and 0xFF)
//                if (sw != 0x9000) return null
//
//                response
//                    .dropLast(2)
//                    .toByteArray()
//                    .joinToString("") { "%02X".format(it) }
//            } finally {
//                card.disconnect(false)
//            }
//        }.getOrNull()
//    }
//}
