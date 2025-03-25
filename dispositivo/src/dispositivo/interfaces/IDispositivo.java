package dispositivo.interfaces;

import java.util.Collection;

public interface IDispositivo {

    public String getId();

    public IDispositivo iniciar();
    public IDispositivo detener();
    
    public IDispositivo addFuncion(IFuncion f);
    public IFuncion getFuncion(String funcionId);
    public Collection<IFuncion> getFunciones();

	//5. Añadir el método isHabilitado para el Dispositivo
    public boolean isHabilitado();
    
    //4. Añadir los métodos habilitar y deshabilitar para el Dispositivo
    public void habilitar();
    public void deshabilitar();
}
