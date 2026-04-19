package com.cultivo.agentes;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Random;

public class AgenteSensorTemperatura extends Agent {

    @Override
    protected void setup() {
        System.out.println("[SensorTemperatura] Iniciando agente: " + getAID().getName());

        // PASO A: Registrarse en páginas amarillas (DF)
        // Esto es como publicar un anuncio: "Soy el sensor de temperatura, búscame aquí"
        DFAgentDescription descripcion = new DFAgentDescription();
        descripcion.setName(getAID()); // Mi identidad

        ServiceDescription servicio = new ServiceDescription();
        servicio.setType("sensor-temperatura");   // tipo de servicio
        servicio.setName("servicio-temperatura"); // nombre del servicio
        descripcion.addServices(servicio);

        try {
            DFService.register(this, descripcion);
            System.out.println("[SensorTemperatura] Registrado en páginas amarillas OK");
        } catch (FIPAException e) {
            System.err.println("[SensorTemperatura] Error al registrar: " + e.getMessage());
        }

        // PASO B: Comportamiento cíclico - esperar mensajes y responder
        // CyclicBehaviour = un bucle infinito que escucha mensajes
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                // Escuchar solo mensajes de tipo REQUEST
                MessageTemplate filtro = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage mensaje = myAgent.receive(filtro);

                if (mensaje != null) {
                    // Alguien nos preguntó la temperatura
                    System.out.println("[SensorTemperatura] Solicitud recibida de: "
                            + mensaje.getSender().getLocalName());

                    // Generar temperatura aleatoria entre 10 y 45 grados
                    Random random = new Random();
                    double temperatura = 10 + (random.nextDouble() * 35);
                    temperatura = Math.round(temperatura * 10.0) / 10.0; // redondear a 1 decimal

                    // Armar la respuesta
                    ACLMessage respuesta = mensaje.createReply(); // createReply ya pone el destino correcto
                    respuesta.setPerformative(ACLMessage.INFORM); // INFORM = "te informo el dato"
                    respuesta.setContent(String.valueOf(temperatura));

                    myAgent.send(respuesta);
                    System.out.println("[SensorTemperatura] Temperatura enviada: " + temperatura + "°C");
                } else {
                    // No hay mensajes, poner el agente en pausa para no consumir CPU
                    block();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        // Al apagarse, desregistrarse de páginas amarillas
        try {
            DFService.deregister(this);
            System.out.println("[SensorTemperatura] Desregistrado de páginas amarillas");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}