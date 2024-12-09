package compilador;

import java.util.ArrayList;

public class Cuadruplos {
    ArrayList <Cuadruplo> cuadruplos = new ArrayList<>();
    Compilador cmp;
    
    public Cuadruplos ( Compilador c ){
        cmp = c;        
    }
    
    public void insertar ( Cuadruplo cua ){
        cuadruplos.add( cua );
    }
    
    public void limpiar (){
        cuadruplos.clear();        
    }
    
    public ArrayList <Cuadruplo> devolverTodos ( ){
        return cuadruplos;
    }
    
    public Cuadruplo devolverCuadruplo ( int i ){
        return cuadruplos.get( i );
    }
    
    public int getTamano(){
        return cuadruplos.size();
    }
}
