package com.zzt.kid.sample

/**
 * A sample payment service that processes transactions.
 * We will hook this class using @EntryHook, @ExitHook, and @Replace.
 */
class PaymentService(val merchantId: String) {

  fun processPayment(amount: Double, currency: String): Boolean {
    println("[PaymentService] Processing $currency $amount for merchant $merchantId")
    // Simulate payment processing
    return amount > 0
  }

  fun refund(transactionId: String, amount: Double) {
    println("[PaymentService] Refunding $amount for transaction $transactionId")
  }

  fun getBalance(): Double {
    println("[PaymentService] Fetching balance for $merchantId")
    return 1000.0
  }
}
