package se.vgregion.messagebus;

import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBus;
import com.liferay.portal.kernel.messaging.MessageListener;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;
import se.vgregion.messagebus.util.SimpleMessageListener;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Bruno Farache
 */
@ContextConfiguration(
        locations = {
                "/META-INF/message-bus-spring-test.xml", "/META-INF/camel-spring-test.xml",
                "/META-INF/test-routes.xml", "/META-INF/jms-messaging.xml"})
public class CamelJMSComponentTest extends AbstractJUnit38SpringContextTests {

    private static final Logger log = Logger.getLogger(CamelJMSComponentTest.class);

    @Value("${messagebus.destination}")
    String messagebusDestination;
    @Value("${activemq.destination}")
    String activemqDestination;

    @DirtiesContext
    public void testJMSMessage() throws Exception {
        SimpleMessageListener.messageReceived = false;
        final String msg = "A test message";

        //Send to SimpleMessageListener
        jmsTemplate.send(activemqDestination, new MessageCreator() {
            @Override
            public javax.jms.Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(msg);
            }
        });

        Thread.sleep(100);

        assertEquals(true, SimpleMessageListener.messageReceived);
        assertEquals(msg, SimpleMessageListener.readMessage);
    }

    @DirtiesContext
    public void testMessageBusToJms() throws Exception {
        SimpleMessageListener.messageReceived = false;
        String payload = "test payload";

        final List<Object> list = sendToMessageBus(payload);

        Thread.sleep(100);

        assertEquals(true, SimpleMessageListener.messageReceived);
        assertEquals(payload, SimpleMessageListener.readMessage);
    }

    @DirtiesContext
    public void testMessageBus() throws Exception {
        String payload = "test payload";
        final List<Object> list = sendToMessageBus(payload);

        Thread.sleep(100);

        assertEquals(1, list.size());
    }

    @DirtiesContext
    public void testCamelQueueReply() throws Exception {
        final String payload = "apa bepa";
        List<String> correlationIds = new ArrayList<String>();
        correlationIds.add(UUID.randomUUID().toString());
        correlationIds.add(UUID.randomUUID().toString());


        Message message = new Message();
        message.setPayload(payload);
        message.setResponseId(correlationIds.get(0));
        Message message2 = new Message();
        message2.setPayload(payload);
        message2.setResponseId(correlationIds.get(1));

        final List<Message> list = new ArrayList();

        messageBus.registerMessageListener(messagebusDestination + ".REPLY", new MessageListener() {
            @Override
            public void receive(Message message) {
                assertEquals(payload.toUpperCase(), message.getPayload());
                list.add(message);
            }
        });

        messageBus.sendMessage(messagebusDestination, message);
        Thread.sleep(10);
        messageBus.sendMessage(messagebusDestination, message2);

        Thread.sleep(1000);

        assertEquals(2, list.size());
        for (Message m : list) {
            assertTrue(correlationIds.contains(m.getResponseId()));
            correlationIds.remove(m.getResponseId());
        }

        assertEquals(0, correlationIds.size());
    }


    private List<Object> sendToMessageBus(final String payload) {
        final List<Object> list = new ArrayList();

        String destinationName = messagebusDestination;

        Message message = new Message();
        message.setPayload(payload);

        messageBus.registerMessageListener(destinationName, new MessageListener() {
            @Override
            public void receive(Message message) {
                assertEquals(payload, message.getPayload());
                list.add(new Object());
            }
        });

        messageBus.sendMessage(destinationName, message);
        return list;
    }

    @Autowired
    private MessageBus messageBus;
    @Autowired
    private JmsTemplate jmsTemplate;

}