package dispositivo.componentes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import dispositivo.api.iot.infraestructure.Dispositivo_RegistradorMQTT;
import dispositivo.api.mqtt.Dispositivo_APIMQTT;
import dispositivo.api.rest.Dispositivo_APIREST;
import dispositivo.interfaces.IDispositivo;
import dispositivo.interfaces.IFuncion;

public class Dispositivo implements IDispositivo {

    protected String deviceId = null;
    private boolean habilitado = true;
    protected Map<String, IFuncion> functions = null;
    protected Dispositivo_RegistradorMQTT registrador = null;
    protected Dispositivo_APIMQTT apiFuncionesMQTT = null;
    protected Dispositivo_APIREST apiFuncionesREST = null;

    public static Dispositivo build(String deviceId, String ip, String mqttBrokerURL) {
        Dispositivo dispositivo = new Dispositivo(deviceId);
        dispositivo.registrador = Dispositivo_RegistradorMQTT.build(deviceId, ip, mqttBrokerURL);
        dispositivo.apiFuncionesMQTT = Dispositivo_APIMQTT.build(dispositivo, mqttBrokerURL);
        dispositivo.apiFuncionesREST = Dispositivo_APIREST.build(dispositivo);
        return dispositivo;
    }

    public static Dispositivo build(String deviceId, String ip, int port, String mqttBrokerURL) {
        Dispositivo dispositivo = new Dispositivo(deviceId);
        dispositivo.registrador = Dispositivo_RegistradorMQTT.build(deviceId, ip, mqttBrokerURL);
        dispositivo.apiFuncionesMQTT = Dispositivo_APIMQTT.build(dispositivo, mqttBrokerURL);
        dispositivo.apiFuncionesREST = Dispositivo_APIREST.build(dispositivo, port);
        return dispositivo;
    }

    protected Dispositivo(String deviceId) {
        this.deviceId = deviceId;
        this.habilitado = true; // Por defecto, el dispositivo está habilitado
    }

    @Override
    public String getId() {
        return this.deviceId;
    }

	//5. Implementar el método isHabilitado para el Dispositivo
    @Override
    public boolean isHabilitado() {
        return this.habilitado;
    }

	//4. Implementar los métodos habilitar y deshabilitar para el Dispositivo
    @Override
    public void habilitar() {
        this.habilitado = true;
        System.out.println("Dispositivo habilitado.");
    }

    @Override
    public void deshabilitar() {
        this.habilitado = false;
        System.out.println("Dispositivo deshabilitado.");
    }

    protected Map<String, IFuncion> getFunctions() {
        return this.functions;
    }

    protected void setFunctions(Map<String, IFuncion> fs) {
        this.functions = fs;
    }

    @Override
    public Collection<IFuncion> getFunciones() {
        if (!this.habilitado) {
            throw new IllegalStateException("El dispositivo está deshabilitado. No se pueden modificar las funciones.");
        }
        if (this.getFunctions() == null)
            return null;
        return this.getFunctions().values();
    }

    @Override
    public IDispositivo addFuncion(IFuncion f) {
        if (!this.isHabilitado()) {
            throw new IllegalStateException("El dispositivo está deshabilitado y no puede modificar funciones.");
        }
        if (this.getFunctions() == null)
            this.setFunctions(new HashMap<String, IFuncion>());
        this.getFunctions().put(f.getId(), f);
        return this;
    }

    @Override
    public IFuncion getFuncion(String funcionId) {
                if (this.getFunctions() == null)
            return null;
        return this.getFunctions().get(funcionId);
    }

    @Override
    public IDispositivo iniciar() {
        if (!this.isHabilitado()) {
            throw new IllegalStateException("El dispositivo está deshabilitado y no puede ser iniciado.");
        }
        for (IFuncion f : this.getFunciones()) {
            f.iniciar();
        }

        this.registrador.registrar();
        this.apiFuncionesMQTT.iniciar();
        this.apiFuncionesREST.iniciar();
        return this;
    }

    @Override
    public IDispositivo detener() {
        if (!this.isHabilitado()) {
            throw new IllegalStateException("El dispositivo está deshabilitado y no puede ser detenido.");
        }
        this.registrador.desregistrar();
        this.apiFuncionesMQTT.detener();
        this.apiFuncionesREST.detener();
        for (IFuncion f : this.getFunciones()) {
            f.detener();
        }
        return this;
    }
}
