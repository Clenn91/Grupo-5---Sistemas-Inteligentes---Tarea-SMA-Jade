package com.cultivo.agentes;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AgenteControlRiego extends Agent {

    @Override
    protected void setup() {
        System.out.println("[ControlRiego] Iniciando agente: " + getAID().getName());
        System.out.println("[ControlRiego] Esperando decisiones del Analizador...");

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                // Escuchar mensajes INFORM con conversationId "decision-riego"
                MessageTemplate filtro = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("decision-riego")
                );

                ACLMessage mensaje = myAgent.receive(filtro);

                if (mensaje != null) {
                    String contenido = mensaje.getContent();

                    // El contenido viene así: "RIEGO_NECESARIO|temp:38.5|hum:22.3"
                    // Lo separamos por "|"
                    String[] partes = contenido.split("\\|");
                    String estado = partes[0];
                    String tempInfo = partes[1].replace("temp:", "");
                    String humInfo  = partes[2].replace("hum:", "");

                    System.out.println("\n[ControlRiego] ====== DECISIÓN RECIBIDA ======");
                    System.out.println("[ControlRiego] Temperatura: " + tempInfo + "°C");
                    System.out.println("[ControlRiego] Humedad:     " + humInfo + "%");

                    // LÓGICA DE NEGOCIO DEL CONTROL DE RIEGO:
                    // Actuar según la decisión del analizador
                    if ("RIEGO_NECESARIO".equals(estado)) {
                        System.out.println("[ControlRiego] ⚠️  ESTADO: ACTIVANDO SISTEMA DE RIEGO ⚠️");
                        System.out.println("[ControlRiego] → Abriendo válvulas de riego...");
                        System.out.println("[ControlRiego] → Duración estimada: 20 minutos");
                    } else {
                        System.out.println("[ControlRiego] ✅  ESTADO: RIEGO NO NECESARIO");
                        System.out.println("[ControlRiego] → Sistema de riego en espera");
                        System.out.println("[ControlRiego] → Próxima revisión en 10 segundos");
                    }
                    System.out.println("[ControlRiego] ================================\n");

                } else {
                    block();
                }
            }
        });
    }
}