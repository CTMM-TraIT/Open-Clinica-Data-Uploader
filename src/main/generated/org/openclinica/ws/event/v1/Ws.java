package org.openclinica.ws.event.v1;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 2.5.1
 * 2016-06-09T17:20:24.427+02:00
 * Generated source version: 2.5.1
 * 
 */
@WebService(targetNamespace = "http://openclinica.org/ws/event/v1", name = "ws")
@XmlSeeAlso({ObjectFactory.class, org.openclinica.ws.beans.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface Ws {

    @WebMethod
    @WebResult(name = "scheduleResponse", targetNamespace = "http://openclinica.org/ws/event/v1", partName = "scheduleResponse")
    public ScheduleResponse schedule(
        @WebParam(partName = "scheduleRequest", name = "scheduleRequest", targetNamespace = "http://openclinica.org/ws/event/v1")
        ScheduleRequest scheduleRequest
    );
}
