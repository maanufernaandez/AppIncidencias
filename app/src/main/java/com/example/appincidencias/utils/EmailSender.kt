package com.example.appincidencias.utils

import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {

    private const val EMAIL = "TU_CORREO@gmail.com"
    private const val PASSWORD = "CONTRASEÑA_DE_APLICACION"

    fun sendEmail(to: String, subject: String, body: String) {

        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(EMAIL, PASSWORD)
            }
        })

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(EMAIL))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            setSubject(subject)
            setText(body)
        }

        Thread { Transport.send(message) }.start()
    }
}
