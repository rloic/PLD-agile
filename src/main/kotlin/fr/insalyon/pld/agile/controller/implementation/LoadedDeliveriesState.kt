package fr.insalyon.pld.agile.controller.implementation

import com.sun.media.sound.InvalidFormatException
import fr.insalyon.pld.agile.Config
import fr.insalyon.pld.agile.controller.api.State
import fr.insalyon.pld.agile.getResource
import fr.insalyon.pld.agile.model.Delivery
import fr.insalyon.pld.agile.model.RoundRequest
import fr.insalyon.pld.agile.service.roundcomputing.implementation.RoundComputerImpl
import fr.insalyon.pld.agile.util.xml.XmlDocument
import fr.insalyon.pld.agile.util.xml.serialization.implementation.DeliverySerializer
import fr.insalyon.pld.agile.util.xml.serialization.implementation.WarehouseSerializer
import fr.insalyon.pld.agile.util.xml.validator.implementation.XmlValidatorImpl
import org.w3c.dom.Element
import org.w3c.dom.NodeList

class LoadedDeliveriesState : DefaultState<RoundRequest>() {

    override fun init(element: RoundRequest, window: Any) {
        println(element)
    }

    override fun loadRoundRequest(controller: Controller) {
        val validator: XmlValidatorImpl = XmlValidatorImpl()
        val xsdFile = getResource(Config.DELIVERY_PLANNING_XSD)
        val file = openXmlFileFromDialog() ?: return

        if (file.extension != "xml") throw InvalidFormatException("The file ${file.name} is not a xml file")
        if (!validator.isValid(file, xsdFile)) throw InvalidFormatException("The file ${file.name} does not match the valid pattern")

        val xmlDocument = XmlDocument.open(file)
        val deliverySerializer = DeliverySerializer(xmlDocument,plan!!)
        val warehouseSerializer = WarehouseSerializer(xmlDocument, plan!!)

        var deliveryNodesList: NodeList = xmlDocument.getElementsByTagName("livraison")
        var deliveriesSet:MutableSet<Delivery> = mutableSetOf()

        for(i in 0 until deliveryNodesList.length) deliveriesSet.add(deliverySerializer.unserialize(deliveryNodesList.item(i) as Element))

        val warehouse =  warehouseSerializer.unserialize(xmlDocument.getElementsByTagName("entrepot")?.item(0) as Element)

        controller.changeStateAndInit(controller.LOADED_DELIVERIES_STATE, RoundRequest(warehouse, deliveriesSet))
    }

    override fun calculateRound(controller: Controller) {
        println("Calculate round was called")

        controller.changeStateAndInit(controller.CALCULATED_ROUND_STATE,RoundComputerImpl(plan!!, roundRequest!!).round)
    }
}
