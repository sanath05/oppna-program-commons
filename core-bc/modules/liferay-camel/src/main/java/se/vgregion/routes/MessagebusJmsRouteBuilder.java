package se.vgregion.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: pabe
 * Date: 2011-04-27
 * Time: 10:29
 */
public class MessagebusJmsRouteBuilder extends SpringRouteBuilder {
    private static Logger log = LoggerFactory.getLogger(MessagebusJmsRouteBuilder.class);

    private String messageBusDestination;
    private String activeMqDestination;

    public MessagebusJmsRouteBuilder(String messageBusDestination, String activeMqDestination) {
        this.messageBusDestination = messageBusDestination;
        this.activeMqDestination = activeMqDestination;

        log.info("MB: {} MQ: {}", messageBusDestination, activeMqDestination);
    }

    @Override
    public void configure() throws Exception {
        from("liferay:" + messageBusDestination)
                .setHeader("JMSCorrelationID", header("responseId"))
                .to("activemq:queue:" + activeMqDestination + "?preserveMessageQos=true&replyTo=" + activeMqDestination + ".REPLY");

        from("activemq:queue:" + activeMqDestination + ".REPLY?disableReplyTo=true")
                .setHeader("responseId", header("JMSCorrelationID"))
                .to("liferay:" + messageBusDestination + ".REPLY");
    }
}
