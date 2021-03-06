package fr.insalyon.pld.agile.model

class Speed constructor(
    val value: Double,
    val distanceUnit: DistanceUnit,
    val durationUnit: DurationUnit
){
    enum class DistanceUnit(val coef: Double) {
      M(1.0),
      DAM(10.0),
      KM(1000.0)
    }
    
    enum class DurationUnit(val coef: Double) {
      H(3600.0),
      M(60.0),
      S(1.0)
    }

  fun to(distanceUnit: DistanceUnit, durationUnit: DurationUnit): Speed {

    val coefDistance = this.distanceUnit.coef / distanceUnit.coef
    val coefTime = this.durationUnit.coef / durationUnit.coef
    return Speed(value * coefDistance / coefTime, distanceUnit, durationUnit)

  }

  fun toMeterPerSeconds(): Speed {
    return to(DistanceUnit.M, DurationUnit.S)
  }

  override fun toString(): String {
    return "$value $distanceUnit/$durationUnit"
  }

}

fun String.toSpeed(): Speed {
  val value = this.split(" ")[0].toDouble()
  val rest = this.split(" ")[1]

  val distanceUnit = rest.split("/")[0]
  val durationUnit = rest.split("/")[1]

  return Speed(
          value,
          Speed.DistanceUnit.valueOf(distanceUnit),
          Speed.DurationUnit.valueOf(durationUnit)
  )

}

val Number.km_h: Speed get() = Speed(toDouble(), Speed.DistanceUnit.KM, Speed.DurationUnit.H)
val Number.m_s: Speed get() = Speed(toDouble(), Speed.DistanceUnit.M, Speed.DurationUnit.S)
