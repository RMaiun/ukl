package com.mairo.ukl.validations

import cats.data.ValidatedNec
import cats.syntax.validated._

trait ValidationRules {
  type ValidationResult[A] = ValidatedNec[String, A]
  type ValidationRule[A] = String => ValidationResult[A]

  def onlyNumbers(field: String): ValidationRule[String] = data =>
    if (data.forall(c => Character.isDigit(c))) {
      data.validNec
    } else s"Field $field must contain only numbers".invalidNec

  def onlyLetters(field: String): ValidationRule[String] = data =>
    if (data.forall(c => Character.isLetter(c))) {
      data.validNec
    } else s"Field $field must contain only letters".invalidNec

  def length(field: String)(from: Int = 0, to: Int = 100): ValidationRule[String] = data =>
    if (data.length >= from && data.length <= to) {
      data.validNec
    } else s"Expected String length [$from,$to] for prop: $field".invalidNec

  def notEmpty(field: String): ValidationRule[String] = data =>
    if (data.nonEmpty) {
      data.validNec
    } else s"Field $field must be present".invalidNec
}
