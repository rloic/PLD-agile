package fr.insalyon.pld.agile.service.roundmodifier.implementaion

import fr.insalyon.pld.agile.Config
import fr.insalyon.pld.agile.model.*
import fr.insalyon.pld.agile.service.algorithm.implementation.TSP1
import fr.insalyon.pld.agile.service.algorithm.implementation.TSP1WithTimeSlot
import fr.insalyon.pld.agile.service.roundcomputing.api.RoundComputer
import fr.insalyon.pld.agile.service.roundcomputing.implementation.RoundComputerImpl
import fr.insalyon.pld.agile.service.roundmodifier.implementation.RoundModifierImp
import fr.insalyon.pld.agile.util.Logger
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

class RoundModifierImpTest {

  val source = Intersection(2, 0, 0)

  val node1 = Intersection(1, 0, 0)
  val node3 = Intersection(3, 0, 0)
  val node4 = Intersection(4, 0, 0)
  val node5 = Intersection(5, 0, 0)
  val node6 = Intersection(6, 0, 0)
  val node7 = Intersection(7, 0, 0)

  val plan by lazy {

    val roadOfLength1 = Junction(1000, "")
    val roadOfLength2 = Junction(2000, "")
    val roadOfLength4 = Junction(4000, "")

    Plan(
        setOf<Intersection>(node1, source, node3, node4, node5, node6, node7),
        setOf(
            //Triple(srcNode, road, destNode)
            Triple(node1, roadOfLength1, source),
            Triple(node1, roadOfLength4, node3),
            Triple(source, roadOfLength1, node4),
            Triple(source, roadOfLength2, node5),
            Triple(node4, roadOfLength1, node3),
            Triple(node4, roadOfLength2, node5),
            Triple(node5, roadOfLength1, node6),
            Triple(node6, roadOfLength1, source),
            Triple(node4, roadOfLength1, node7),
            Triple(node7, roadOfLength1, node6)

        )
    )
  }

  val roundRequest = RoundRequest(
      Warehouse(address = source, departureHour = 8 h 0 m 0),
      setOf(
          Delivery(node4, duration = 200.seconds),
          Delivery(node5, duration = 400.seconds),
          Delivery(node6, duration = 100.seconds)
      )
  )

  val newPlan = """
      ${Config.Business.DEFAULT_SPEED}
      1;2;3;4
      1 -> 2 : 15
      2 -> 3 : 15
      3 -> 4 : 20
      4 -> 1 : 10
      """.toPlan()

  @Test
  fun removeDeliveryAtFirstPosition() {
    val roundComputer: RoundComputer = RoundComputerImpl(plan, roundRequest, tsp = TSP1WithTimeSlot(roundRequest), speed = 1.m_s)
    val round = roundComputer.round

    val roundModifier = RoundModifierImp(plan)
    Logger.debug(round.toTrace())

    roundModifier.removeDelivery(0, round)

    assertEquals(2, round.deliveries().size)
    assertEquals(2000, round.distancePathInMeters().elementAt(0).length)
    assertEquals(5, round.deliveries().elementAt(0).address.id)

  }

  @Test
  fun removeDeliveryTestNotAtFirstPosition() {
    val roundComputer: RoundComputer = RoundComputerImpl(plan, roundRequest, tsp = TSP1WithTimeSlot(roundRequest), speed = 1.m_s)
    val round = roundComputer.round

    val roundModifier = RoundModifierImp(plan)
    roundModifier.removeDelivery(1, round)

    assertEquals(2000, round.distancePathInMeters().elementAt(1).length)
    assertEquals(4, round.deliveries().elementAt(0).address.id)
    assertEquals(6, round.deliveries().elementAt(1).address.id)
  }

  @Test
  fun removeDeliveryTestLastPosition() {
    val roundComputer: RoundComputer = RoundComputerImpl(plan, roundRequest, tsp = TSP1WithTimeSlot(roundRequest), speed = 1.m_s)
    val round = roundComputer.round

    val roundModifier = RoundModifierImp(plan)
    roundModifier.removeDelivery(2, round)

    Assert.assertEquals("2 -> 4 -> 5 -> 2", round.toString())

    Assert.assertEquals(2, round.deliveries().size)
    Assert.assertEquals(4, round.deliveries().elementAt(0).address.id)
    Assert.assertEquals(5, round.deliveries().elementAt(1).address.id)
  }

  @Test
  fun getEarliestEndTimeTest() {
    val source = Intersection(2, 0, 0)

    val node1 = Intersection(1, 0, 0)
    val node3 = Intersection(3, 0, 0)
    val node4 = Intersection(4, 0, 0)

    val plan by lazy {

      val roadOfLength15 = Junction(900, "")
      val roadOfLength20 = Junction(1200, "")
      val roadOfLength10 = Junction(600, "")

      Plan(
          setOf<Intersection>(node1, source, node3, node4, node5, node6, node7),
          setOf(
              //Triple(srcNode, road, destNode)
              Triple(source, roadOfLength15, node1),
              Triple(node1, roadOfLength15, node3),
              Triple(node3, roadOfLength20, node4),
              Triple(node4, roadOfLength10, source)

          )
      )
    }

    val roundRequest = RoundRequest(
        Warehouse(address = source, departureHour = 8 h 0 m 0),
        setOf(
            Delivery(node1, duration = 600.seconds, startTime = 8 h 10 m 0),
            Delivery(node3, duration = 600.seconds, startTime = 8 h 45 m 0, endTime = 10 h 0),
            Delivery(node4, duration = 600.seconds, startTime = 9 h 0 m 0, endTime = 11 h 0)
        )
    )

    val roundComputer: RoundComputer = RoundComputerImpl(plan, roundRequest, tsp = TSP1WithTimeSlot(roundRequest), speed = 1.m_s)
    val round = roundComputer.round

    val roundModifier = RoundModifierImp(plan)
    var listEarliestEndTime = roundModifier.getEarliestEndTime(round)

    Logger.debug(listEarliestEndTime)

  }

  @Test
  fun getLatestEndTimeTest() {
    val source = Intersection(2, 0, 0)

    val node1 = Intersection(1, 0, 0)
    val node3 = Intersection(3, 0, 0)
    val node4 = Intersection(4, 0, 0)

    val plan by lazy {

      val roadOfLength15 = Junction(900, "")
      val roadOfLength20 = Junction(1200, "")
      val roadOfLength10 = Junction(600, "")

      Plan(
          setOf<Intersection>(node1, source, node3, node4, node5, node6, node7),
          linkedSetOf(
              //Triple(srcNode, road, destNode)
              Triple(source, roadOfLength15, node1),
              Triple(node1, roadOfLength15, node3),
              Triple(node3, roadOfLength20, node4),
              Triple(node4, roadOfLength10, source)

          )
      )
    }

    val roundRequest = RoundRequest(
        Warehouse(address = source, departureHour = 8 h 0 m 0),
        linkedSetOf(
            Delivery(node1, duration = 600.seconds, startTime = 8 h 10 m 0),
            Delivery(node3, duration = 600.seconds, startTime = 8 h 45 m 0, endTime = 10 h 0),
            Delivery(node4, duration = 600.seconds, startTime = 9 h 0 m 0, endTime = 11 h 0)
        )
    )

    val roundComputer: RoundComputer = RoundComputerImpl(plan, roundRequest, tsp = TSP1WithTimeSlot(roundRequest), speed = 1.m_s)
    val round = roundComputer.round

    val roundModifier = RoundModifierImp(plan)
    //var listLatestEndTime = roundModifier.getLastestEndTime(round)

    //Logger.debug(listLatestEndTime)
  }

  //Values for testing the modification of a delivery
  val planBis by lazy {

    val roadOfLength15 = Junction(15.minutes.toSeconds(), "")
    val roadOfLength20 = Junction(20.minutes.toSeconds(), "")
    val roadOfLength10 = Junction(10.minutes.toSeconds(), "")

    Plan(
        setOf<Intersection>(node1, source, node3, node4, node5, node6, node7),
        setOf(
            //Triple(srcNode, road, destNode)
            Triple(source, roadOfLength15, node1),
            Triple(node1, roadOfLength15, node3),
            Triple(node3, roadOfLength20, node4),
            Triple(node4, roadOfLength10, source)

        )
    )
  }

  val roundRequestBis = RoundRequest(
      Warehouse(address = source, departureHour = 8 h 0 m 0),
      setOf(
          Delivery(node1, duration = 10.minutes, startTime = 8 h 10 m 0),
          Delivery(node3, duration = 10.minutes, startTime = 8 h 45 m 0, endTime = 10 h 0),
          Delivery(node4, duration = 10.minutes, startTime = 9 h 0 m 0, endTime = 11 h 0)
      )
  )

  @Test
  fun roundModifyCheckCanStartAt8h40() {

    val roundComputer: RoundComputer = RoundComputerImpl(planBis, roundRequestBis, tsp = TSP1WithTimeSlot(roundRequest), speed = 1.m_s)
    val round = roundComputer.round

    Logger.debug(round.toTrace())
    val roundModifier = RoundModifierImp(planBis)
    roundModifier.modifyDelivery(Delivery(address = node3, startTime = 8 h 40, duration = 600.seconds), round, 1)

    Assert.assertEquals(8 h 40, round.deliveries().elementAt(1).startTime)
  }

  @Test
  fun roundModifyCheckMustFinishMaxAt10h30() {
    val roundComputer: RoundComputer = RoundComputerImpl(planBis, roundRequestBis, tsp = TSP1WithTimeSlot(roundRequest), speed = 15.km_h)
    val round = roundComputer.round

    val roundModifier = RoundModifierImp(planBis)
    Logger.debug(round.toTrace())
    Assert.assertEquals(10 h 0, round.deliveries().elementAt(1).endTime)
    roundModifier.modifyDelivery(Delivery(address = node1, startTime = 8 h 40, duration = 600.seconds, endTime = 10 h 30), round, 1)

    Assert.assertEquals(10 h 30, round.deliveries().elementAt(1).endTime)
  }

  @Test(expected = IllegalArgumentException::class)
  fun roundModifyCheckDurationGreatherThanPreviousAndNextConstraints() {

    val roundComputer: RoundComputer = RoundComputerImpl(planBis, roundRequestBis, tsp = TSP1WithTimeSlot(roundRequest), speed = 1.m_s)
    val round = roundComputer.round

    val roundModifier = RoundModifierImp(planBis)

    Assert.assertEquals(600.seconds, round.deliveries().elementAt(1).duration)
    roundModifier.modifyDelivery(Delivery(address = node3, duration = 3.hours), round, 1)

    Assert.assertEquals(600.seconds, round.deliveries().elementAt(1).duration)
  }

  @Test
  fun roundModifyCheckDurationWithRespectToConstraints() {

    val roundComputer: RoundComputer = RoundComputerImpl(planBis, roundRequestBis, tsp = TSP1WithTimeSlot(roundRequest), speed = 1.m_s)
    val round = roundComputer.round

    val roundModifier = RoundModifierImp(planBis)

    Assert.assertEquals(600.seconds, round.deliveries().elementAt(1).duration)
    roundModifier.modifyDelivery(Delivery(address = node3, duration = 1800.seconds), round, 1)

    Assert.assertEquals(1800.seconds, round.deliveries().elementAt(1).duration)
  }

  @Test
  fun roundModifyFirstElementCanStartAt8h15() {
    val roundComputer: RoundComputer = RoundComputerImpl(planBis, roundRequestBis, tsp = TSP1WithTimeSlot(roundRequest), speed = 1.m_s)
    val round = roundComputer.round

    val roundModifier = RoundModifierImp(planBis)

    Assert.assertEquals(600.seconds, round.deliveries().elementAt(1).duration)
    roundModifier.modifyDelivery(Delivery(address = node3, startTime = 8 h 15, duration = 600.seconds), round, 0)
    Assert.assertEquals(8 h 15, round.deliveries().elementAt(0).startTime)
  }

  // values to test add delivery
  val planAddDelivery by lazy {

    val roadOfLength15 = Junction((900 * Config.Business.DEFAULT_SPEED.toMeterPerSeconds().value).toInt(), "")
    val roadOfLength20 = Junction((1200 * Config.Business.DEFAULT_SPEED.toMeterPerSeconds().value).toInt(), "")
    val roadOfLength10 = Junction((600 * Config.Business.DEFAULT_SPEED.toMeterPerSeconds().value).toInt(), "")
    val roadOfLength8 = Junction((480 * Config.Business.DEFAULT_SPEED.toMeterPerSeconds().value).toInt(), "")

    Plan(
        setOf<Intersection>(node1, source, node3, node4, node5, node6, node7),
        setOf(
            //Triple(srcNode, road, destNode)
            Triple(source, roadOfLength15, node1),
            Triple(node1, roadOfLength15, node3),
            Triple(node3, roadOfLength20, node4),
            Triple(node4, roadOfLength10, source),
            Triple(node1, roadOfLength8, node7),
            Triple(node7, roadOfLength8, node1),
            Triple(node7, roadOfLength8, node3),
            Triple(node3, roadOfLength8, node7),
            Triple(node4, roadOfLength8, node7),
            Triple(node7, roadOfLength8, node4),
            Triple(node7, roadOfLength8, source),
            Triple(source, roadOfLength8, node7)
        )
    )
  }

  val roundRequestAddDelivery = RoundRequest(
      Warehouse(address = source, departureHour = 8 h 0 m 0),
      setOf(
          Delivery(node1, duration = 600.seconds, startTime = 8 h 10 m 0),
          Delivery(node3, duration = 600.seconds, startTime = 8 h 45 m 0, endTime = 10 h 0),
          Delivery(node4, duration = 600.seconds, startTime = 9 h 0 m 0, endTime = 11 h 0)
      )
  )

  @Test
  fun roundAddDeliveryInFirstPlace() {
    val roundComputer: RoundComputer = RoundComputerImpl(planAddDelivery, roundRequestAddDelivery, tsp = TSP1(), speed = Config.Business.DEFAULT_SPEED)
    val round = roundComputer.round

    val roundModifier = RoundModifierImp(planAddDelivery)

    Assert.assertEquals(600.seconds, round.deliveries().elementAt(0).duration)
    roundModifier.addDelivery(Delivery(address = node7, duration = 600.seconds), round)
    Assert.assertEquals(7, round.deliveries().elementAt(0).address.id)

  }

  @Test
  fun roundAddDeliveryInSecondPlace() {

    val plan = """
            ${Config.Business.DEFAULT_SPEED}
            1;2;3;4;5;6;7
            2 -> 1 : 15
            2 -> 7 : 8
            7 -> 1 : 8
            1 -> 7 : 4
            7 -> 3 : 4
            3 -> 7 : 8
            7 -> 4 : 8
            1 -> 3 : 15
            3 -> 4 : 20
            4 -> 2 : 10
            """.toPlan()

    val roundComputer: RoundComputer = RoundComputerImpl(plan, roundRequestAddDelivery, tsp = TSP1(), speed = Config.Business.DEFAULT_SPEED)
    val round = roundComputer.round

    val roundModifier = RoundModifierImp(plan)

    Logger.debug(round.toTrace())

    Assert.assertEquals(600.seconds, round.deliveries().elementAt(0).duration)
    roundModifier.addDelivery(Delivery(address = node7, duration = 600.seconds), round)

    Logger.debug(round.toTrace())

    Assert.assertEquals(7, round.deliveries().elementAt(1).address.id)

  }

  @Test
  fun roundAddDeliveryWithEndTimeConstraint() {

    val plan = """
            ${Config.Business.DEFAULT_SPEED}
            1;2;3;4;5;6;7
            2 -> 1 : 15
            2 -> 7 : 8
            7 -> 1 : 8
            1 -> 7 : 4
            7 -> 3 : 4
            3 -> 7 : 8
            7 -> 4 : 8
            1 -> 3 : 15
            3 -> 4 : 20
            4 -> 2 : 10
            """.toPlan()

    val roundComputer: RoundComputer = RoundComputerImpl(plan, roundRequestAddDelivery, tsp = TSP1(), speed = Config.Business.DEFAULT_SPEED)
    val round = roundComputer.round

    val roundModifier = RoundModifierImp(plan)

    Logger.debug(round.toTrace())

    Assert.assertEquals(600.seconds, round.deliveries().elementAt(0).duration)
    roundModifier.addDelivery(Delivery(address = node7, endTime = 8 h 30, duration = 600.seconds), round)

    Logger.debug(round.toTrace())

    Assert.assertEquals(7, round.deliveries().elementAt(0).address.id)


  }

}