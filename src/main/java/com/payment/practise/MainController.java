package com.payment.practise;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Controller
public class MainController {

    @Autowired
    private JavaMailSender mailSender;

    @PostMapping("/user/create_order")
    @ResponseBody
    public String createOrder(@RequestBody Map<String, Object> data) throws Exception {
        System.out.println("Received Data: " + data);

        int amt = Integer.parseInt(data.get("amount").toString());
        String name = data.get("name").toString();
        String email = data.get("email").toString();
        String date = data.get("date").toString();
        String timeSlot = data.get("timeSlot").toString();

        RazorpayClient razorpayClient = new RazorpayClient("rzp_live_Feu7veD5q1rRUz", "Khe5N8pxjDRdyAL1YNEVvmR9");

        JSONObject options = new JSONObject();
        options.put("amount", amt * 100);
        options.put("currency", "INR");
        options.put("receipt", "txn_" + System.currentTimeMillis());

        // Creating a new order
        Order order = razorpayClient.orders.create(options);
        System.out.println("Created Order: " + order);

        return order.toString(); // Send order details to the frontend
    }

    @PostMapping("/user/verify_payment")
    @ResponseBody
    public String verifyPayment(@RequestBody Map<String, Object> data) {
        System.out.println("Payment Verification Data: " + data);

        try {
            String razorpayOrderId = data.get("razorpay_order_id").toString();
            String razorpayPaymentId = data.get("razorpay_payment_id").toString();
            String razorpaySignature = data.get("razorpay_signature").toString();

            // Verify payment signature
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);

            RazorpayClient razorpayClient = new RazorpayClient("rzp_live_Feu7veD5q1rRUz", "Khe5N8pxjDRdyAL1YNEVvmR9");
            boolean isValid = Utils.verifyPaymentSignature(options, "Khe5N8pxjDRdyAL1YNEVvmR9");

            if (isValid) {
                // Payment is valid, send confirmation email
                String name = data.get("name").toString();
                String email = data.get("email").toString();
                String date = data.get("date").toString();
                String timeSlot = data.get("timeSlot").toString();
                String amount = data.get("amount").toString();

                sendEmail(name, email, date, timeSlot, razorpayPaymentId, amount);
                return "Payment successful! Booking confirmation email sent.";
            } else {
                return "Payment verification failed!";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error verifying payment.";
        }
    }

    private void sendEmail(String name, String to, String date, String timeSlot, String transactionId, String amount) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("Your Slot Booking Confirmation");

            String emailContent = "<h3>Dear " + name + ",</h3>" +
                    "<p>We are delighted to confirm your booking.</p>" +
                    "<p><strong>Booking Details:</strong></p>" +
                    "<ul>" +
                    "<li><strong>Date:</strong> " + date + "</li>" +
                    "<li><strong>Time Slot:</strong> " + timeSlot + "</li>" +
                    "<li><strong>Amount Paid:</strong> INR " + amount + "</li>" +
                    "<li><strong>Transaction ID:</strong> " + transactionId + "</li>" +
                    "</ul>" +
                    "<p>Thank you for choosing our services. If you have any questions or need further assistance, please feel free to reach out.</p>" +
                    "<p>We look forward to serving you!</p>" +
                    "<p>Best regards,</p>" +
                    "<p><strong>-Mahapranalika</strong></p>";

            helper.setText(emailContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/bookslot")
    public void hello(HttpServletResponse response) throws IOException {
        response.sendRedirect("/index.html");
    }
}
