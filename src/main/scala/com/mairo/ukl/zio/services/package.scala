package com.mairo.ukl.zio

import com.mairo.ukl.dtos.AddPlayerDto
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

package object services {
  case class SeasonDto(name:String)
  case class FoundSeasonsDto(itemList: List[SeasonDto])



  object codecs{
    implicit val seasonDtoDecoder: Decoder[SeasonDto] = deriveDecoder[SeasonDto]
    implicit val seasonDtoEncoder: Encoder[SeasonDto] = deriveEncoder[SeasonDto]

    implicit val foundSeasonsDtoDecoder: Decoder[FoundSeasonsDto] = deriveDecoder[FoundSeasonsDto]
    implicit val foundSeasonsDtoEncoder: Encoder[FoundSeasonsDto] = deriveEncoder[FoundSeasonsDto]
  }
}
