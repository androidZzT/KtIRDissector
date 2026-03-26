package com.zzt.kid.sample

import com.zzt.kid.annotation.EntryHook
import com.zzt.kid.annotation.ExitHook
import com.zzt.kid.annotation.Replace
import com.zzt.kid.runtime.MethodHook

/**
 * Hook definitions for PaymentService.
 *
 * Convention:
 * - Hook methods must be defined inside an `object` (singleton).
 * - First parameter is always the receiver instance (the object being hooked).
 * - Remaining parameters match the original method's parameter list.
 *
 * @EntryHook: Called at method entry. Return MethodHook.pass() to continue,
 *             or MethodHook.intercept(value) to skip the original method.
 * @ExitHook:  Called after the original method completes. Cannot change return value.
 * @Replace:   Completely replaces the original method body.
 */
object PaymentHooks {

  // ── @EntryHook Example ──────────────────────────────────────────────────────
  // Validates payment amount before processing. Intercepts if amount is invalid.

  @EntryHook(
    className = "com.zzt.kid.sample.PaymentService",
    methodName = "processPayment",
    paramsTypes = "(kotlin.Doublekotlin.String)"
  )
  fun validateBeforePayment(
    service: PaymentService,
    amount: Double,
    currency: String
  ): MethodHook<Boolean> {
    if (amount <= 0) {
      println("[EntryHook] Invalid amount $amount — intercepting payment")
      return MethodHook.intercept(false)  // Skip original, return false
    }
    if (currency !in listOf("USD", "EUR", "CNY")) {
      println("[EntryHook] Unsupported currency $currency — intercepting payment")
      return MethodHook.intercept(false)
    }
    println("[EntryHook] Pre-check passed for $currency $amount")
    return MethodHook.pass()  // Proceed to original method
  }

  // ── @ExitHook Example ───────────────────────────────────────────────────────
  // Logs after a refund completes (useful for audit trails).

  @ExitHook(
    className = "com.zzt.kid.sample.PaymentService",
    methodName = "refund",
    paramsTypes = "(kotlin.Stringkotlin.Double)"
  )
  fun auditAfterRefund(service: PaymentService, transactionId: String, amount: Double) {
    println("[ExitHook] Audit log: refund of $amount completed for tx=$transactionId, merchant=${service.merchantId}")
  }

  // ── @Replace Example ─────────────────────────────────────────────────────────
  // Replaces getBalance() with a mock value (useful for testing or feature flags).

  @Replace(
    className = "com.zzt.kid.sample.PaymentService",
    methodName = "getBalance",
    paramsTypes = "()"
  )
  fun mockGetBalance(service: PaymentService): Double {
    println("[Replace] Returning mock balance for ${service.merchantId}")
    return 9999.0  // Mocked balance
  }
}
