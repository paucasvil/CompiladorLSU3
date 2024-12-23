/*:-----------------------------------------------------------------------------
 *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
 *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
 *:                         LENGUAJES Y AUTOMATAS II           
 *: 
 *:        SEMESTRE: ______________            HORA: ______________ HRS
 *:                                   
 *:               
 *:    # Clase con la funcionalidad del Generador de COdigo Objeto
 *                 
 *:                           
 *: Archivo       : GenCodigoInt.java
 *: Autor         : Fernando Gil  
 *: Fecha         : 03/SEP/2014
 *: Compilador    : Java JDK 7
 *: Descripción   :  
 *:                  
 *:           	     
 *: Ult.Modif.    :
 *:  Fecha      Modificó            Modificacion
 *:=============================================================================
 *: 24/May/2023 F.Gil              -Generar la plantilla de programa Ensamblador
 *:-----------------------------------------------------------------------------
 */


package compilador;

import general.Linea_TS;


public class GenCodigoObj {
 
    private Compilador cmp;

    
    //--------------------------------------------------------------------------
    // Constructor de la clase, recibe la referencia de la clase principal del 
    // compilador.
    //
    public GenCodigoObj ( Compilador c ) {
        cmp = c;
    }
    // Fin del Constructor
    //--------------------------------------------------------------------------
	
    public void generar () {
        genEncabezadoASM ();
        genDeclaraVarsASM();
        genSegmentoCodigo();
        algoritmoGCO     ();
        genPieASM        ();
    }    

    //--------------------------------------------------------------------------
    // Genera las primeras lineas del programa Ensamblador hasta antes de la 
    // declaracion de variables.
    
    private void genEncabezadoASM () {
        cmp.iuListener.mostrarCodObj ( "TITLE CodigoObjeto ( codigoObjeto.asm )"  );
        cmp.iuListener.mostrarCodObj ( "; Descripción del programa: Automatas II" );
        cmp.iuListener.mostrarCodObj ( "; Fecha de creacion: Ago-Dic/2024"        );
        cmp.iuListener.mostrarCodObj ( "; Revisiones:" );
        cmp.iuListener.mostrarCodObj ( "; Autores: Paulina Castañeda (21130850)" );
        cmp.iuListener.mostrarCodObj ( ";          Layla González    (21130868)" );
        cmp.iuListener.mostrarCodObj ( "; Fecha de ult. modificacion:" );
        cmp.iuListener.mostrarCodObj ( "" );
        cmp.iuListener.mostrarCodObj ( "; INCLUDE Irvine32.inc" );
        cmp.iuListener.mostrarCodObj ( "; (aqui se insertan las definiciones de simbolos)" );
        cmp.iuListener.mostrarCodObj ( "" );
        cmp.iuListener.mostrarCodObj ( ".model small" );
        cmp.iuListener.mostrarCodObj ( ".stack 4096h" );
        cmp.iuListener.mostrarCodObj ( ".data" );
        cmp.iuListener.mostrarCodObj ( "  " + "cad DB 'Hola Mundo$'" );
        cmp.iuListener.mostrarCodObj ( "");
        cmp.iuListener.mostrarCodObj ( "  ; (aqui se insertan las variables)" );        
    }
    
    //--------------------------------------------------------------------------
    // Genera las lineas en Ensamblador de Declaracion de variables.
    // Todas las variables serán DWORD ya que por simplificacion solo se genera
    // codigo objeto de programas fuente que usaran solo variables enteras.
    
    private void genDeclaraVarsASM () {
        for ( int i = 1; i < cmp.ts.getTamaño (); i++ ) {
            // Por cada entrada en la Tabla de Simbolos...
            Linea_TS elemento = cmp.ts.obt_elemento( i );
            String variable = elemento.getLexema();            
            
            // Genera una declaracion de variable solo si se trata de un id
            if ( elemento.getComplex().equals ( "id" ) ) 
                cmp.iuListener.mostrarCodObj ( "  " + variable + " DW 0" );                       
        }
        cmp.iuListener.mostrarCodObj ( "" );
    }
    
    //--------------------------------------------------------------------------
    
    private void genSegmentoCodigo () {
        cmp.iuListener.mostrarCodObj ( ".code" );
        cmp.iuListener.mostrarCodObj ( "main PROC" );
        cmp.iuListener.mostrarCodObj ( "  " + "mov ax, @Data" );
        cmp.iuListener.mostrarCodObj ( "  " + "mov ds, ax" );
        cmp.iuListener.mostrarCodObj ( "  ; imprimir la leyenda Hola mundo" );
        cmp.iuListener.mostrarCodObj ( "  " + "MOV ah,09h" );
        cmp.iuListener.mostrarCodObj ( "  " + "MOV dx,offset cad" );
        cmp.iuListener.mostrarCodObj ( "  " + "INT 21h" );
        cmp.iuListener.mostrarCodObj ( "  ; (aqui se insertan las instrucciones ejecutables)" );
    }
    
    //--------------------------------------------------------------------------
    // Genera las lineas en Ensamblador de finalizacion del programa
    
    private void genPieASM () {
        cmp.iuListener.mostrarCodObj ( "  " + "mov ax,4c00h" );
        cmp.iuListener.mostrarCodObj ( "  " + "int 21h" );
        cmp.iuListener.mostrarCodObj ( "main ENDP" );
        cmp.iuListener.mostrarCodObj ( "" );
        cmp.iuListener.mostrarCodObj ( "; (aqui se insertan los procedimientos adicionales)" );
        cmp.iuListener.mostrarCodObj ( "END main" );
    }
    
    //--------------------------------------------------------------------------
    // Algoritmo de generacion de codigo en ensamblador
    // Aqui se implementa la traducción de cada cuadruplo en su equivalente en
    // lenguaje ensamblador.
    private void algoritmoGCO () {       
        for(int i = 0; i < cmp.cua.getTamano(); i++){
            Cuadruplo cuas = cmp.cua.devolverCuadruplo(i);
            String op = cuas.getOp();
            if( op.equals("+") || op.equals("*")){
                cmp.iuListener.mostrarCodObj( "  " +"; traduccion de " + cuas.getResultado() + 
                    " := " +cuas.getArg1()+ " " + cuas.getOp() + " " + cuas.getArg2() );
            }else if( op.equals( ":=" ) ) {
                cmp.iuListener.mostrarCodObj( "  " +"; traduccion de " + cuas.getResultado() + 
                    " := " +cuas.getArg1() );
                cmp.iuListener.mostrarCodObj ( "  " + "mov ax, " + cuas.getArg1() );
            }
            
            
            if(cuas.getOp().equals("+")){
                cmp.iuListener.mostrarCodObj ( "  " + "mov ax, " + cuas.getArg1() );
                cmp.iuListener.mostrarCodObj ( "  " + "add ax, " + cuas.getArg2() );
            }else if(cuas.getOp().equals("*")){
                cmp.iuListener.mostrarCodObj ( "  " + "mov bx, " + cuas.getArg2() );
                cmp.iuListener.mostrarCodObj ( "  " + "mov ax, " + cuas.getArg1() );
                cmp.iuListener.mostrarCodObj ( "  " + "mul bx" );
            }
            cmp.iuListener.mostrarCodObj ( "  " + "mov " + cuas.getResultado() + ", ax");
        }
    }
    
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    
}
