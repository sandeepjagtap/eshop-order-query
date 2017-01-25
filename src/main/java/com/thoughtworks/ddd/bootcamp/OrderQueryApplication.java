package com.thoughtworks.ddd.bootcamp;

import com.rabbitmq.client.Channel;
import com.thoughtworks.domain.event.OrderCreatedEvent;
import com.thoughtworks.domain.event.OrderItemAssociatedToSKUEvent;
import com.thoughtworks.domain.model.Item;
import com.thoughtworks.domain.model.OrderView;
import org.axonframework.amqp.eventhandling.DefaultAMQPMessageConverter;
import org.axonframework.amqp.eventhandling.spring.SpringAMQPMessageSource;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
@EnableCouchbaseRepositories(basePackageClasses = {OrderQueryRepository.class})

public class OrderQueryApplication extends AbstractCouchbaseConfiguration {

	private final static Logger LOG = LoggerFactory.getLogger(OrderQueryApplication.class);


	public static void main(String[] args) {
		SpringApplication.run(OrderQueryApplication.class, args);
	}

	@Override
	protected List<String> getBootstrapHosts() {
		return Collections.singletonList("127.0.0.1");
	}

	@Override
	protected String getBucketName() {
		return "orders";
	}

	@Override
	protected String getBucketPassword() {
		return "";
	}

	@Component
	@ProcessingGroup("orderQuery")
	public static class OrderUpdater {

		private OrderQueryRepository repository;

		public OrderUpdater(OrderQueryRepository repository) {
			this.repository = repository;
		}

		@EventHandler
		public void handle(OrderCreatedEvent event) {
			repository.save(new OrderView(event.getId(), event.getItems()));
		}

		@EventHandler
		public void handle(OrderItemAssociatedToSKUEvent event){
			OrderView order = repository.findOne(event.getOrderId());

			Optional<Item> itemStream = order.getItems().stream().filter(item -> item.getIdentifier().equals(event.getItemId())).findFirst();
			itemStream.ifPresent( item ->  item.associateSKUs(event.getSkus()));

			LOG.info("Handling OrderItemAssociatedToSKUEvent" + event);
			repository.save(order);
			LOG.info("OrderView saved is" + order);
		}
	}

	@Bean
	public SpringAMQPMessageSource orderSource(Serializer serlializer) {

		return new SpringAMQPMessageSource(new DefaultAMQPMessageConverter(serlializer)) {
			@RabbitListener(queues = "OrderReadEvents")
			public void onMessage(Message message, Channel channel) throws Exception {
				super.onMessage(message, channel);
			}
		};
	}


	@Bean
	public Exchange exchange() {
		return ExchangeBuilder.fanoutExchange("appFanoutExchange").build();
	}

	@Bean
	public Queue queue() {
		return QueueBuilder.durable("OrderReadEvents").build();
	}

	@Bean
	public Binding binding() {
		return BindingBuilder.bind(queue()).to(exchange()).with("*").noargs();
	}

	@Autowired
	public void configure(AmqpAdmin admin) {
		admin.declareQueue(queue());
		admin.declareBinding(binding());
	}
}
