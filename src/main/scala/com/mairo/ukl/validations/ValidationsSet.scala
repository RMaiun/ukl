package com.mairo.ukl.validations

import com.mairo.ukl.dtos._
import com.wix.accord.dsl._
import com.wix.accord.transform.ValidationTransform.TransformedValidator


object ValidationsSet extends CustomValidationRules {

  implicit val ListLastRoundsValidator: TransformedValidator[FindLastRoundsDto] = validator[FindLastRoundsDto] { dto =>
    dto.season is season
    dto.qty is intBetween(1, 10_000)
  }

  implicit val AddRoundDtoValidator: TransformedValidator[AddRoundDto] = validator[AddRoundDto] { dto =>
    dto.w1 is notEmpty and onlyLetters
    dto.w2 is notEmpty and onlyLetters
    dto.l1 is notEmpty and onlyLetters
    dto.l2 is notEmpty and onlyLetters
    dto.moderator is notEmpty and onlyNumbers
  }

  implicit val SeasonDtoValidator: TransformedValidator[SeasonDto] = validator[SeasonDto] { dto =>
    dto.season is season
  }

  implicit val AddPlayerDtoValidator: TransformedValidator[AddPlayerDto] = validator[AddPlayerDto] { dto =>
    dto.moderator is notEmpty and onlyNumbers
    dto.surname is onlyLetters and sizeBetween(2, 20)
  }

  implicit val LinkTidValidator: TransformedValidator[LinkTidDto] = validator[LinkTidDto] { dto =>
    dto.moderator is notEmpty and onlyNumbers
    dto.nameToLink is onlyLetters and sizeBetween(2, 20)
    dto.tid is notEmpty and onlyNumbers
  }

  implicit val SubscriptionActionValidator: TransformedValidator[SeasonDto] = validator[SeasonDto] { dto =>
    dto.season is season
  }
}
