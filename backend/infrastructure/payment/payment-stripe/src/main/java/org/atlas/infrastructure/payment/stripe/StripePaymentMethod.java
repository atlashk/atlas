package org.atlas.infrastructure.payment.stripe;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <a
 * href="https://docs.stripe.com/api/payment_methods/object#payment_method_object-type">Supported
 * Stripe Payment Method Types</a>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum StripePaymentMethod {

  ACSS_DEBIT("acss_debit"),
  AFFIRM("affirm"),
  AFTERPAY_CLEARPAY("afterpay_clearpay"),
  ALIPAY("alipay"),
  ALMA("alma"),
  AMAZON_PAY("amazon_pay"),
  AU_BECS_DEBIT("au_becs_debit"),
  BACS_DEBIT("bacs_debit"),
  BANCONTACT("bancontact"),
  BILLIE("billie"),
  BLIK("blik"),
  BOLETO("boleto"),
  CARD("card"),
  CARD_PRESENT("card_present"),
  CASHAPP("cashapp"),
  CRYPTO("crypto"),
  CUSTOMER_BALANCE("customer_balance"),
  EPS("eps"),
  FPX("fpx"),
  GIROPAY("giropay"),
  GRABPAY("grabpay"),
  IDEAL("ideal"),
  INTERAC_PRESENT("interac_present"),
  KAKAO_PAY("kakao_pay"),
  KLARNA("klarna"),
  KONBINI("konbini"),
  KR_CARD("kr_card"),
  LINK("link"),
  MOBILEPAY("mobilepay"),
  MULTIBANCO("multibanco"),
  NAVER_PAY("naver_pay"),
  NZ_BANK_ACCOUNT("nz_bank_account"),
  OXXO("oxxo"),
  P24("p24"),
  PAY_BY_BANK("pay_by_bank"),
  PAYCO("payco"),
  PAYNOW("paynow"),
  PAYPAL("paypal"),
  PIX("pix"),
  PROMPTPAY("promptpay"),
  REVOLUT_PAY("revolut_pay"),
  SAMSUNG_PAY("samsung_pay"),
  SATISPAY("satispay"),
  SEPA_DEBIT("sepa_debit"),
  SOFORT("sofort"),
  SWISH("swish"),
  TWINT("twint"),
  US_BANK_ACCOUNT("us_bank_account"),
  WECHAT_PAY("wechat_pay"),
  ZIP("zip");

  private final String type;
}
