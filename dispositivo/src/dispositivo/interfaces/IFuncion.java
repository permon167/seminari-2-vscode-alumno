package dispositivo.interfaces;

public interface IFuncion {
	
	public String getId();
	
	public IFuncion iniciar();
	public IFuncion detener();
	
	public IFuncion encender();
	public IFuncion apagar();
	public IFuncion parpadear();
	
	//3. Añadir el método getStatus para la Funcion
	public FuncionStatus getStatus();

	default void setStatus(String estado) {
		System.out.println("Estado actualizado a: " + estado);
	}
}
