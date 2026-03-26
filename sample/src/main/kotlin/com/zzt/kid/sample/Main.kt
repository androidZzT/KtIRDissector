package com.zzt.kid.sample

/**
 * Sample application demonstrating KtIRDissector (@EntryHook, @ExitHook, @Replace).
 *
 * All hooks in PaymentHooks are applied at compile time — zero runtime reflection overhead.
 */
fun main() {
  val service = PaymentService("merchant-001")

  println("=== @EntryHook Demo: validateBeforePayment ===")
  // Valid payment: hook passes through to original method
  val result1 = service.processPayment(100.0, "USD")
  println("Result: $result1\n")

  // Invalid amount: hook intercepts and returns false
  val result2 = service.processPayment(-50.0, "USD")
  println("Result: $result2\n")

  // Unsupported currency: hook intercepts and returns false
  val result3 = service.processPayment(200.0, "JPY")
  println("Result: $result3\n")

  println("=== @ExitHook Demo: auditAfterRefund ===")
  // Original refund runs, then audit hook logs after completion
  service.refund("tx-12345", 50.0)
  println()

  println("=== @Replace Demo: mockGetBalance ===")
  // Original getBalance() is completely replaced by the mock
  val balance = service.getBalance()
  println("Balance: $balance")
}
