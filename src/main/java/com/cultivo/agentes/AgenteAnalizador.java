package com.cultivo.agentes;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AgenteAnalizador extends Agent {

    // Variables compartidas entre comportamientos
    private AID sensorTemperatura;
    private AID sensorHumedad;
    private AID controlRiego;
    private double temperaturaActual;
    private double humedadActual;

    @Override
    protected void setup() {
        System.out.println("[Analizador] Iniciando agente: " + getAID().getName());

        // Esperar 3 segundos antes de empezar para dar tiempo a que los sensores se registren
        // Luego repetir el análisis cada 10 segundos (10000 ms)
        addBehaviour(new jade.core.behaviours.WakerBehaviour(this, 8000) {
            @Override
            protected void onWake() {
                // Después de 8s, agregar el ticker que repite cada 10s
                myAgent.addBehaviour(new TickerBehaviour(myAgent, 10000) {
                    @Override
                    protected void onTick() {
                        System.out.println("\n[Analizador] ========== NUEVO CICLO DE ANÁLISIS ==========");
                        ejecutarCicloAnalisis();
                    }
                });
            }
        });
    }

    private void ejecutarCicloAnalisis() {
        // SequentialBehaviour ejecuta los pasos UNO POR UNO en orden
        SequentialBehaviour ciclo = new SequentialBehaviour(this);

        // PASO 1: Buscar sensores en páginas amarillas
        ciclo.addSubBehaviour(new Behaviour(this) {
            private boolean terminado = false;

            @Override
            public void action() {
                System.out.println("[Analizador] Buscando sensores en páginas amarillas...");

                // Buscar sensor de temperatura
                sensorTemperatura = buscarEnPaginasAmarillas("sensor-temperatura");
                // Buscar sensor de humedad
                sensorHumedad = buscarEnPaginasAmarillas("sensor-humedad");
                // Buscar agente de control de riego por nombre directo
                controlRiego = new AID("controlRiego", AID.ISLOCALNAME);

                if (sensorTemperatura != null && sensorHumedad != null) {
                    System.out.println("[Analizador] Sensores encontrados:");
                    System.out.println("  - Temperatura: " + sensorTemperatura.getLocalName());
                    System.out.println("  - Humedad: " + sensorHumedad.getLocalName());
                } else {
                    System.out.println("[Analizador] ERROR: No se encontraron los sensores");
                }
                terminado = true;
            }

            @Override
            public boolean done() {
                return terminado;
            }
        });

        // PASO 2: Pedir temperatura al sensor
        ciclo.addSubBehaviour(new Behaviour(this) {
            private boolean terminado = false;

            @Override
            public void action() {
                if (sensorTemperatura == null) {
                    terminado = true;
                    return;
                }

                // Enviar REQUEST al sensor de temperatura
                ACLMessage solicitud = new ACLMessage(ACLMessage.REQUEST);
                solicitud.addReceiver(sensorTemperatura);
                solicitud.setContent("dame-temperatura");
                solicitud.setConversationId("consulta-temp");
                myAgent.send(solicitud);
                System.out.println("[Analizador] Solicitud de temperatura enviada...");

                // Esperar la respuesta
                MessageTemplate filtro = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("consulta-temp")
                );
                ACLMessage respuesta = myAgent.receive(filtro);

                if (respuesta != null) {
                    temperaturaActual = Double.parseDouble(respuesta.getContent());
                    System.out.println("[Analizador] Temperatura recibida: " + temperaturaActual + "°C");
                    terminado = true;
                } else {
                    block(); // esperar sin consumir CPU
                }
            }

            @Override
            public boolean done() {
                return terminado;
            }
        });

        // PASO 3: Pedir humedad al sensor
        ciclo.addSubBehaviour(new Behaviour(this) {
            private boolean terminado = false;

            @Override
            public void action() {
                if (sensorHumedad == null) {
                    terminado = true;
                    return;
                }

                ACLMessage solicitud = new ACLMessage(ACLMessage.REQUEST);
                solicitud.addReceiver(sensorHumedad);
                solicitud.setContent("dame-humedad");
                solicitud.setConversationId("consulta-hum");
                myAgent.send(solicitud);
                System.out.println("[Analizador] Solicitud de humedad enviada...");

                MessageTemplate filtro = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("consulta-hum")
                );
                ACLMessage respuesta = myAgent.receive(filtro);

                if (respuesta != null) {
                    humedadActual = Double.parseDouble(respuesta.getContent());
                    System.out.println("[Analizador] Humedad recibida: " + humedadActual + "%");
                    terminado = true;
                } else {
                    block();
                }
            }

            @Override
            public boolean done() {
                return terminado;
            }
        });

        // PASO 4: Analizar datos y enviar decisión al control de riego
        ciclo.addSubBehaviour(new Behaviour(this) {
            private boolean terminado = false;

            @Override
            public void action() {
                // LÓGICA DE NEGOCIO DEL ANALIZADOR:
                // Riego necesario si temperatura alta O humedad baja
                boolean riegoNecesario = (temperaturaActual > 35.0) || (humedadActual < 30.0);

                String estadoRiego = riegoNecesario ? "RIEGO_NECESARIO" : "SIN_RIEGO";
                String contenido = estadoRiego + "|temp:" + temperaturaActual + "|hum:" + humedadActual;

                System.out.println("[Analizador] Análisis completo:");
                System.out.println("  Temperatura: " + temperaturaActual + "°C (umbral: >35°C)");
                System.out.println("  Humedad:     " + humedadActual + "% (umbral: <30%)");
                System.out.println("  Decisión:    " + estadoRiego);

                // Enviar resultado al agente de control de riego
                ACLMessage mensajeDecision = new ACLMessage(ACLMessage.INFORM);
                mensajeDecision.addReceiver(controlRiego);
                mensajeDecision.setContent(contenido);
                mensajeDecision.setConversationId("decision-riego");
                myAgent.send(mensajeDecision);
                System.out.println("[Analizador] Decisión enviada al ControlRiego");

                terminado = true;
            }

            @Override
            public boolean done() {
                return terminado;
            }
        });

        addBehaviour(ciclo);
    }

    // Método auxiliar para buscar agentes en páginas amarillas por tipo de servicio
    private AID buscarEnPaginasAmarillas(String tipoServicio) {
        DFAgentDescription plantilla = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(tipoServicio);
        plantilla.addServices(sd);

        try {
            // Usa el DF por defecto de la plataforma (no hardcodear df@jade-p1 ni URLs MTP)
            DFAgentDescription[] resultados = DFService.search(this, plantilla);

            if (resultados != null && resultados.length > 0) {
                return resultados[0].getName();
            } else {
                System.out.println("[Analizador] Servicio no encontrado: " + tipoServicio);
            }
        } catch (FIPAException e) {
            System.err.println("[Analizador] Error buscando " + tipoServicio + ": " + e.getMessage());
        }
        return null;
    }
}