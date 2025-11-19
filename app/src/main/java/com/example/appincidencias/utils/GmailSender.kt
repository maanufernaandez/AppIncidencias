package com.example.appincidencias.utils

import java.util.Properties
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class GmailSender(
    private val email: String,
    private val password: String
) {

    fun sendEmail(to: String, subject: String, body: String) {
        val props = Properties()

        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.socketFactory.port"] = "465"
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.port"] = "465"

        val session = Session.getDefaultInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(email, password)
            }
        })

        val message = MimeMessage(session)
        message.setFrom(InternetAddress(email))
        message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(to))
        message.subject = subject
        message.setText(body)

        Transport.send(message)
    }
}
