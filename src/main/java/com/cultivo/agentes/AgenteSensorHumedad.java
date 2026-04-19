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

public class AgenteSensorHumedad extends Agent {

    @Override
    protected void setup() {
        System.out.println("[SensorHumedad] Iniciando agente: " + getAID().getName());

        // Registrarse en páginas amarillas con servicio "sensor-humedad"
        DFAgentDescription descripcion = new DFAgentDescription();
        descripcion.setName(getAID());

        ServiceDescription servicio = new ServiceDescription();
        servicio.setType("sensor-humedad");
        servicio.setName("servicio-humedad");
        descripcion.addServices(servicio);

        try {
            DFService.register(this, descripcion);
            System.out.println("[SensorHumedad] Registrado en páginas amarillas OK");
        } catch (FIPAException e) {
            System.err.println("[SensorHumedad] Error al registrar: " + e.getMessage());
        }

        // Comportamiento: escuchar solicitudes y responder con humedad aleatoria
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                MessageTemplate filtro = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage mensaje = myAgent.receive(filtro);

                if (mensaje != null) {
                    System.out.println("[SensorHumedad] Solicitud recibida de: "
                            + mensaje.getSender().getLocalName());

                    // Generar humedad aleatoria entre 0% y 100%
                    Random random = new Random();
                    double humedad = random.nextDouble() * 100;
                    humedad = Math.round(humedad * 10.0) / 10.0;

                    ACLMessage respuesta = mensaje.createReply();
                    respuesta.setPerformative(ACLMessage.INFORM);
                    respuesta.setContent(String.valueOf(humedad));

                    myAgent.send(respuesta);
                    System.out.println("[SensorHumedad] Humedad enviada: " + humedad + "%");
                } else {
                    block();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
            System.out.println("[SensorHumedad] Desregistrado de páginas amarillas");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}