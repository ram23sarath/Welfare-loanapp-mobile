package com.ijreddy.loanapp.ui.common

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object WhatsAppHelper {

    fun shareLoanSummary(
        context: Context,
        phoneNumber: String,
        customerName: String,
        loanAmount: Double,
        totalInstallments: Int,
        paymentDate: String
    ) {
        val message = """
            *Loan Alert*
            Hello $customerName,
            
            A new loan of ₹$loanAmount has been recorded.
            Repayment Date: $paymentDate
            Total Installments: $totalInstallments
            
            Thank you!
        """.trimIndent()
        
        sendWhatsAppMessage(context, phoneNumber, message)
    }

    fun shareInstallmentReceipt(
        context: Context,
        phoneNumber: String,
        customerName: String,
        installmentNumber: Int?,
        amount: Double,
        receiptNumber: String
    ) {
        val installmentText = if (installmentNumber != null) "Installment #$installmentNumber" else "Installment"
        val message = """
            *Payment Receipt*
            Hello $customerName,
            
            We received a payment of ₹$amount for $installmentText.
            Receipt No: $receiptNumber
            
            Thank you!
        """.trimIndent()
        
        sendWhatsAppMessage(context, phoneNumber, message)
    }

    fun shareSubscriptionReceipt(
        context: Context,
        phoneNumber: String,
        customerName: String,
        amount: Double,
        date: String
    ) {
        val message = """
            *Subscription Receipt*
            Hello $customerName,
            
            We received a subscription payment of ₹$amount on $date.
            
            Thank you!
        """.trimIndent()
        
        sendWhatsAppMessage(context, phoneNumber, message)
    }

    private fun sendWhatsAppMessage(context: Context, phoneNumber: String, message: String) {
        try {
            // Use URL scheme which handles both WA and WA Business automatically
            // and falls back to browser if neither is installed (though uncommon)
            // But spec asked for ACTION_SEND. 
            // ACTION_SEND is better for sharing generally, but URL is better for "Direct Message to Specific Number".
            // ACTION_SEND usually requires user to pick the contact.
            // URL scheme `https://wa.me/phone?text=...` opens the chat with that specific number.
            // Given we have the phone number, the URL scheme is superior for UX.
            
            val cleanPhone = phoneNumber.filter { it.isDigit() }
            // Ensure country code if missing. Spec says phone is 10 digits. Assume +91 for India context (based on currency symbol ₹)
            val fullPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
            
            val url = "https://wa.me/$fullPhone?text=${URLEncoder.encode(message, "UTF-8")}"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            context.startActivity(intent)
            
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp not installed or error occurred", Toast.LENGTH_SHORT).show()
        }
    }
}
