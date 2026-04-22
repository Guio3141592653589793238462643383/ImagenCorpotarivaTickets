package com.austral.back.services.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarRespuestaTicket(String para, String nombre, String mensaje, Integer ticketId) {

        String asunto = "Respuesta a tu Ticket #" + ticketId;

        String contenido = """
                <html>
                  <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                      @import url('https://fonts.googleapis.com/css2?family=Lora:wght@400;600&family=DM+Sans:wght@300;400;500&display=swap');
                      * { margin: 0; padding: 0; box-sizing: border-box; }
                      body {
                        font-family: 'DM Sans', Helvetica, sans-serif;
                        background-color: #F0EFEB;
                        padding: 40px 16px;
                        -webkit-font-smoothing: antialiased;
                      }
                      .wrapper { max-width: 620px; margin: 0 auto; }
                      .header {
                        background-color: #1C1C1A;
                        padding: 28px 40px;
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        border-radius: 8px 8px 0 0;
                      }
                      .header img { max-height: 32px; width: auto; }
                      .header-label {
                        font-family: 'DM Sans', sans-serif;
                        font-size: 11px;
                        font-weight: 500;
                        letter-spacing: 0.12em;
                        text-transform: uppercase;
                        color: #8A8A82;
                      }
                      .body {
                        background-color: #FFFFFF;
                        padding: 48px 40px;
                        border-left: 1px solid #E5E4DF;
                        border-right: 1px solid #E5E4DF;
                      }
                      .eyebrow {
                        font-size: 11px;
                        font-weight: 500;
                        letter-spacing: 0.12em;
                        text-transform: uppercase;
                        color: #9A9A92;
                        margin-bottom: 12px;
                      }
                      h1 {
                        font-family: 'Lora', Georgia, serif;
                        font-size: 26px;
                        font-weight: 600;
                        color: #1C1C1A;
                        line-height: 1.3;
                        margin-bottom: 28px;
                      }
                      p {
                        font-size: 15px;
                        line-height: 1.7;
                        color: #4A4A45;
                        margin-bottom: 16px;
                      }
                      .divider {
                        border: none;
                        border-top: 1px solid #E5E4DF;
                        margin: 28px 0;
                      }
                      .message-block {
                        background-color: #F7F7F5;
                        border-left: 3px solid #1C1C1A;
                        border-radius: 0 6px 6px 0;
                        padding: 20px 24px;
                        margin: 24px 0;
                        font-size: 15px;
                        line-height: 1.7;
                        color: #3A3A35;
                      }
                      .signature {
                        font-size: 14px;
                        color: #6A6A62;
                        margin-top: 32px;
                        line-height: 1.6;
                      }
                      .signature strong { color: #1C1C1A; font-weight: 500; }
                      .footer {
                        background-color: #F0EFEB;
                        padding: 24px 40px;
                        border: 1px solid #E5E4DF;
                        border-top: none;
                        border-radius: 0 0 8px 8px;
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                      }
                      .footer-brand {
                        font-size: 12px;
                        font-weight: 500;
                        color: #9A9A92;
                        letter-spacing: 0.06em;
                        text-transform: uppercase;
                      }
                      .footer-meta {
                        font-size: 11px;
                        color: #AEADA6;
                      }
                      @media (max-width: 600px) {
                        .header, .body, .footer { padding: 24px 20px; }
                        h1 { font-size: 22px; }
                        .footer { flex-direction: column; gap: 8px; text-align: center; }
                      }
                    </style>
                  </head>
                  <body>
                    <div class="wrapper">
                      <div class="header">
                        <img src="https://www.australlens.com/images/logo-austral.png" alt="Austral Lens">
                        <span class="header-label">Soporte técnico</span>
                      </div>

                      <div class="body">
                        <p class="eyebrow">Ticket #%3$d</p>
                        <h1>Respuesta a tu solicitud</h1>

                        <p>Hola <strong style="color:#1C1C1A; font-weight:500;">%1$s</strong>,</p>
                        <p>Nuestro equipo ha respondido tu ticket. A continuación encontrarás el mensaje:</p>

                        <div class="message-block">%2$s</div>

                        <hr class="divider">

                        <div class="signature">
                          Saludos cordiales,<br>
                          <strong>Equipo Austral Lens</strong>
                        </div>
                      </div>

                      <div class="footer">
                        <span class="footer-brand">Austral Lens</span>
                        <span class="footer-meta">Cra. 40 #20a-25 &nbsp;·&nbsp; Ticket #%3$d</span>
                      </div>
                    </div>
                  </body>
                </html>
                """.formatted(nombre, mensaje, ticketId);

        enviarCorreo(para, asunto, contenido);
    }

    public void enviarRecuperacionPassword(String para, String nombre, String token) {

        String asunto = "Recuperación de contraseña - Centro de Tickets";
        String enlace = baseUrl + "/reset-password?token=" + token;

        String contenido = """
                <html>
                  <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                      @import url('https://fonts.googleapis.com/css2?family=Lora:wght@400;600&family=DM+Sans:wght@300;400;500&display=swap');
                      * { margin: 0; padding: 0; box-sizing: border-box; }
                      body {
                        font-family: 'DM Sans', Helvetica, sans-serif;
                        background-color: #F0EFEB;
                        padding: 40px 16px;
                        -webkit-font-smoothing: antialiased;
                      }
                      .wrapper { max-width: 620px; margin: 0 auto; }
                      .header {
                        background-color: #1C1C1A;
                        padding: 28px 40px;
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        border-radius: 8px 8px 0 0;
                      }
                      .header-brand {
                        font-family: 'DM Sans', sans-serif;
                        font-size: 14px;
                        font-weight: 500;
                        color: #FFFFFF;
                        letter-spacing: 0.02em;
                      }
                      .header-label {
                        font-size: 11px;
                        font-weight: 500;
                        letter-spacing: 0.12em;
                        text-transform: uppercase;
                        color: #6A6A62;
                      }
                      .body {
                        background-color: #FFFFFF;
                        padding: 48px 40px;
                        border-left: 1px solid #E5E4DF;
                        border-right: 1px solid #E5E4DF;
                      }
                      .eyebrow {
                        font-size: 11px;
                        font-weight: 500;
                        letter-spacing: 0.12em;
                        text-transform: uppercase;
                        color: #9A9A92;
                        margin-bottom: 12px;
                      }
                      h1 {
                        font-family: 'Lora', Georgia, serif;
                        font-size: 26px;
                        font-weight: 600;
                        color: #1C1C1A;
                        line-height: 1.3;
                        margin-bottom: 28px;
                      }
                      p {
                        font-size: 15px;
                        line-height: 1.7;
                        color: #4A4A45;
                        margin-bottom: 16px;
                      }
                      .btn-container { text-align: center; margin: 36px 0; }
                      .btn {
                        display: inline-block;
                        background-color: #1C1C1A;
                        color: #FFFFFF !important;
                        text-decoration: none;
                        padding: 14px 36px;
                        border-radius: 6px;
                        font-size: 14px;
                        font-weight: 500;
                        letter-spacing: 0.04em;
                        font-family: 'DM Sans', Helvetica, sans-serif;
                      }
                      .note {
                        font-size: 13px;
                        color: #AEADA6;
                        line-height: 1.6;
                        text-align: center;
                      }
                      .divider {
                        border: none;
                        border-top: 1px solid #E5E4DF;
                        margin: 28px 0;
                      }
                      .footer {
                        background-color: #F0EFEB;
                        padding: 20px 40px;
                        border: 1px solid #E5E4DF;
                        border-top: none;
                        border-radius: 0 0 8px 8px;
                        text-align: center;
                        font-size: 11px;
                        color: #AEADA6;
                        letter-spacing: 0.04em;
                      }
                      @media (max-width: 600px) {
                        .header, .body, .footer { padding: 24px 20px; }
                        h1 { font-size: 22px; }
                        .btn { padding: 12px 28px; font-size: 13px; }
                      }
                    </style>
                  </head>
                  <body>
                    <div class="wrapper">
                      <div class="header">
                        <span class="header-brand">Centro de Soporte TI</span>
                        <span class="header-label">Seguridad</span>
                      </div>

                      <div class="body">
                        <p class="eyebrow">Solicitud de acceso</p>
                        <h1>Recuperación de contraseña</h1>

                        <p>Hola <strong style="color:#1C1C1A; font-weight:500;">%s</strong>,</p>
                        <p>Recibimos una solicitud para restablecer la contraseña de tu cuenta. Haz clic en el botón para continuar:</p>

                        <div class="btn-container">
                          <a href="%s" class="btn">Restablecer contraseña</a>
                        </div>

                        <hr class="divider">

                        <p class="note">
                          Si no solicitaste este cambio, puedes ignorar este correo.<br>
                          Este enlace expirará en 24 horas.
                        </p>
                      </div>

                      <div class="footer">
                        Centro de Tickets — Mesa de ayuda interna
                      </div>
                    </div>
                  </body>
                </html>
                """.formatted(nombre, enlace);

        enviarCorreo(para, asunto, contenido);
    }

    public void enviarTicketCerrado(String correo, String nombre, Integer ticketId) {

        String asunto = "Tu ticket ha sido cerrado #" + ticketId;

        String contenido = """
                <html>
                  <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                      @import url('https://fonts.googleapis.com/css2?family=Lora:wght@400;600&family=DM+Sans:wght@300;400;500&display=swap');
                      * { margin: 0; padding: 0; box-sizing: border-box; }
                      body {
                        font-family: 'DM Sans', Helvetica, sans-serif;
                        background-color: #F0EFEB;
                        padding: 40px 16px;
                        -webkit-font-smoothing: antialiased;
                      }
                      .wrapper { max-width: 620px; margin: 0 auto; }
                      .header {
                        background-color: #1C1C1A;
                        padding: 28px 40px;
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        border-radius: 8px 8px 0 0;
                      }
                      .header img { max-height: 32px; width: auto; }
                      .header-label {
                        font-size: 11px;
                        font-weight: 500;
                        letter-spacing: 0.12em;
                        text-transform: uppercase;
                        color: #8A8A82;
                      }
                      .body {
                        background-color: #FFFFFF;
                        padding: 48px 40px;
                        border-left: 1px solid #E5E4DF;
                        border-right: 1px solid #E5E4DF;
                        text-align: center;
                      }
                      .status-badge {
                        display: inline-block;
                        background-color: #F0EFEB;
                        border: 1px solid #D5D4CF;
                        border-radius: 100px;
                        padding: 6px 16px;
                        font-size: 11px;
                        font-weight: 500;
                        letter-spacing: 0.1em;
                        text-transform: uppercase;
                        color: #6A6A62;
                        margin-bottom: 20px;
                      }
                      .status-badge::before {
                        content: "●";
                        color: #5A8A5A;
                        margin-right: 6px;
                        font-size: 9px;
                      }
                      h1 {
                        font-family: 'Lora', Georgia, serif;
                        font-size: 26px;
                        font-weight: 600;
                        color: #1C1C1A;
                        line-height: 1.3;
                        margin-bottom: 24px;
                      }
                      p {
                        font-size: 15px;
                        line-height: 1.7;
                        color: #4A4A45;
                        margin-bottom: 16px;
                      }
                      .ticket-ref {
                        display: inline-block;
                        font-size: 13px;
                        font-weight: 500;
                        color: #1C1C1A;
                        background-color: #F7F7F5;
                        border: 1px solid #E5E4DF;
                        border-radius: 4px;
                        padding: 4px 10px;
                      }
                      .divider {
                        border: none;
                        border-top: 1px solid #E5E4DF;
                        margin: 28px 0;
                      }
                      .signature {
                        font-size: 14px;
                        color: #6A6A62;
                        line-height: 1.6;
                      }
                      .signature strong { color: #1C1C1A; font-weight: 500; }
                      .footer {
                        background-color: #F0EFEB;
                        padding: 20px 40px;
                        border: 1px solid #E5E4DF;
                        border-top: none;
                        border-radius: 0 0 8px 8px;
                        text-align: center;
                        font-size: 11px;
                        color: #AEADA6;
                        letter-spacing: 0.04em;
                      }
                      @media (max-width: 600px) {
                        .header, .body, .footer { padding: 24px 20px; }
                        h1 { font-size: 22px; }
                      }
                    </style>
                  </head>
                  <body>
                    <div class="wrapper">
                      <div class="header">
                        <img src="https://www.australlens.com/images/logo-austral.png" alt="Austral Lens">
                        <span class="header-label">Soporte técnico</span>
                      </div>

                      <div class="body">
                        <span class="status-badge">Resuelto</span>
                        <h1>Tu ticket ha sido cerrado</h1>

                        <p>Hola <strong style="color:#1C1C1A; font-weight:500;">%s</strong>,</p>
                        <p>
                          El ticket <span class="ticket-ref">#%d</span> ha sido marcado como
                          cerrado por el equipo de soporte.
                        </p>
                        <p>Si el problema persiste, puedes abrir un nuevo ticket desde el sistema en cualquier momento.</p>

                        <hr class="divider">

                        <div class="signature">
                          Saludos cordiales,<br>
                          <strong>Equipo Austral Lens</strong>
                        </div>
                      </div>

                      <div class="footer">
                        Austral Lens — Sistema de soporte técnico
                      </div>
                    </div>
                  </body>
                </html>
                """.formatted(nombre, ticketId);

        enviarCorreo(correo, asunto, contenido);
    }

    public void enviarVerificacionCuenta(String para, String nombre, String token) {

        String asunto = "Verifica tu cuenta - Austral Lens";
        String link = baseUrl + "/verificar?token=" + token;

        String contenido = """
                <html>
                  <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                      @import url('https://fonts.googleapis.com/css2?family=Lora:wght@400;600&family=DM+Sans:wght@300;400;500&display=swap');
                      * { margin: 0; padding: 0; box-sizing: border-box; }
                      body {
                        font-family: 'DM Sans', Helvetica, sans-serif;
                        background-color: #F0EFEB;
                        padding: 40px 16px;
                        -webkit-font-smoothing: antialiased;
                      }
                      .wrapper { max-width: 620px; margin: 0 auto; }
                      .header {
                        background-color: #1C1C1A;
                        padding: 28px 40px;
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        border-radius: 8px 8px 0 0;
                      }
                      .header img { max-height: 32px; width: auto; }
                      .header-label {
                        font-size: 11px;
                        font-weight: 500;
                        letter-spacing: 0.12em;
                        text-transform: uppercase;
                        color: #8A8A82;
                      }
                      .body {
                        background-color: #FFFFFF;
                        padding: 48px 40px;
                        border-left: 1px solid #E5E4DF;
                        border-right: 1px solid #E5E4DF;
                      }
                      .eyebrow {
                        font-size: 11px;
                        font-weight: 500;
                        letter-spacing: 0.12em;
                        text-transform: uppercase;
                        color: #9A9A92;
                        margin-bottom: 12px;
                      }
                      h1 {
                        font-family: 'Lora', Georgia, serif;
                        font-size: 26px;
                        font-weight: 600;
                        color: #1C1C1A;
                        line-height: 1.3;
                        margin-bottom: 28px;
                      }
                      p {
                        font-size: 15px;
                        line-height: 1.7;
                        color: #4A4A45;
                        margin-bottom: 16px;
                      }
                      .btn-container { text-align: center; margin: 36px 0; }
                      .btn {
                        display: inline-block;
                        background-color: #1C1C1A;
                        color: #FFFFFF !important;
                        text-decoration: none;
                        padding: 14px 36px;
                        border-radius: 6px;
                        font-size: 14px;
                        font-weight: 500;
                        letter-spacing: 0.04em;
                        font-family: 'DM Sans', Helvetica, sans-serif;
                      }
                      .note {
                        font-size: 13px;
                        color: #AEADA6;
                        line-height: 1.6;
                        text-align: center;
                      }
                      .divider {
                        border: none;
                        border-top: 1px solid #E5E4DF;
                        margin: 28px 0;
                      }
                      .signature {
                        font-size: 14px;
                        color: #6A6A62;
                        line-height: 1.6;
                        margin-top: 28px;
                      }
                      .signature strong { color: #1C1C1A; font-weight: 500; }
                      .footer {
                        background-color: #F0EFEB;
                        padding: 20px 40px;
                        border: 1px solid #E5E4DF;
                        border-top: none;
                        border-radius: 0 0 8px 8px;
                        text-align: center;
                        font-size: 11px;
                        color: #AEADA6;
                        letter-spacing: 0.04em;
                      }
                      @media (max-width: 600px) {
                        .header, .body, .footer { padding: 24px 20px; }
                        h1 { font-size: 22px; }
                        .btn { padding: 12px 28px; font-size: 13px; }
                      }
                    </style>
                  </head>
                  <body>
                    <div class="wrapper">
                      <div class="header">
                        <img src="https://www.australlens.com/images/logo-austral.png" alt="Austral Lens">
                        <span class="header-label">Nueva cuenta</span>
                      </div>

                      <div class="body">
                        <p class="eyebrow">Bienvenido</p>
                        <h1>Verifica tu cuenta</h1>

                        <p>Hola <strong style="color:#1C1C1A; font-weight:500;">%s</strong>,</p>
                        <p>Gracias por registrarte. Para activar tu cuenta haz clic en el siguiente botón:</p>

                        <div class="btn-container">
                          <a href="%s" class="btn">Verificar mi cuenta</a>
                        </div>

                        <hr class="divider">

                        <p class="note">
                          Si no creaste esta cuenta, puedes ignorar este correo.<br>
                          Este enlace expira en 24 horas.
                        </p>

                        <div class="signature">
                          Saludos cordiales,<br>
                          <strong>Equipo Austral Lens</strong>
                        </div>
                      </div>

                      <div class="footer">
                        Austral Lens
                      </div>
                    </div>
                  </body>
                </html>
                """.formatted(nombre, link);

        enviarCorreo(para, asunto, contenido);
    }

    private void enviarCorreo(String correo, String asunto, String contenido) {

        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(correo);
            helper.setSubject(asunto);
            helper.setText(contenido, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar correo", e);
        }
    }
}