
package Modelo;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileSystemView;

public class PedidosDao {
    Connection con;
    Conexion cn = new Conexion();
    PreparedStatement ps;
    ResultSet rs;
    int r;
    
    public int IdPedido(){
        int id = 0;
        String sql = "SELECT MAX(id) FROM pedidos";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return id;
    }
    
    public int verificarStado(int mesa, int id_sala){
        int id_pedido = 0;
        String sql = "SELECT id FROM pedidos WHERE num_mesa=? AND id_sala=? AND estado = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, mesa);
            ps.setInt(2, id_sala);
            ps.setString(3, "PENDIENTE");
            rs = ps.executeQuery();
            if(rs.next()){
                id_pedido = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return id_pedido;
    }
    
    public int RegistrarPedido(Pedidos ped){
        String sql = "INSERT INTO pedidos (id_sala, num_mesa, total, usuario) VALUES (?,?,?,?)";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, ped.getId_sala());
            ps.setInt(2, ped.getNum_mesa());
            ps.setDouble(3, ped.getTotal());
            ps.setString(4, ped.getUsuario());
            ps.execute();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }finally{
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
        }
        return r;
    }
    
    public int RegistrarDetalle(DetallePedido det){
       String sql = "INSERT INTO detalle_pedidos (nombre, precio, cantidad, comentario, id_pedido) VALUES (?,?,?,?,?)";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, det.getNombre());
            ps.setDouble(2, det.getPrecio());
            ps.setInt(3, det.getCantidad());
            ps.setString(4, det.getComentario());
            ps.setInt(5, det.getId_pedido());
            ps.execute();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return r;
    }
    
    public List verPedidoDetalle(int id_pedido){
       List<DetallePedido> Lista = new ArrayList();
       String sql = "SELECT d.* FROM pedidos p INNER JOIN detalle_pedidos d ON p.id = d.id_pedido WHERE p.id = ?";
       try {
           con = cn.getConnection();
           ps = con.prepareStatement(sql);
           ps.setInt(1, id_pedido);
           rs = ps.executeQuery();
           while (rs.next()) {               
               DetallePedido det = new DetallePedido();
               det.setId(rs.getInt("id"));
               det.setNombre(rs.getString("nombre"));
               det.setPrecio(rs.getDouble("precio"));
               det.setCantidad(rs.getInt("cantidad"));
               det.setComentario(rs.getString("comentario"));
               Lista.add(det);
           }
       } catch (SQLException e) {
           System.out.println(e.toString());
       }
       return Lista;
   }
    
    public Pedidos verPedido(int id_pedido){
        Pedidos ped = new Pedidos();
       String sql = "SELECT p.*, s.nombre FROM pedidos p INNER JOIN salas s ON p.id_sala = s.id WHERE p.id = ?";
       try {
           con = cn.getConnection();
           ps = con.prepareStatement(sql);
           ps.setInt(1, id_pedido);
           rs = ps.executeQuery();
            if (rs.next()) {               
               
               ped.setId(rs.getInt("id"));
               ped.setFecha(rs.getString("fecha"));
               ped.setSala(rs.getString("nombre"));
               ped.setNum_mesa(rs.getInt("num_mesa"));
               ped.setTotal(rs.getDouble("total"));
           }
       } catch (SQLException e) {
           System.out.println(e.toString());
       }
       return ped;
   }
    
    public List finalizarPedido(int id_pedido){
       List<DetallePedido> Lista = new ArrayList();
       String sql = "SELECT d.* FROM pedidos p INNER JOIN detalle_pedidos d ON p.id = d.id_pedido WHERE p.id = ?";
       try {
           con = cn.getConnection();
           ps = con.prepareStatement(sql);
           ps.setInt(1, id_pedido);
           rs = ps.executeQuery();
           while (rs.next()) {               
               DetallePedido det = new DetallePedido();
               det.setId(rs.getInt("id"));
               det.setNombre(rs.getString("nombre"));
               det.setPrecio(rs.getDouble("precio"));
               det.setCantidad(rs.getInt("cantidad"));
               det.setComentario(rs.getString("comentario"));
               Lista.add(det);
           }
       } catch (SQLException e) {
           System.out.println(e.toString());
       }
       return Lista;
   }
    
    public void pdfPedido(int id_pedido) {
        String fechaPedido = null, usuario = null, total = null,
                sala = null, num_mesa = null;
        try {
            FileOutputStream archivo;
            String url = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
            File salida = new File(url + File.separator + "pedido.pdf");
            archivo = new FileOutputStream(salida);
            Document doc = new Document(new Rectangle(226.77f, 283.46f));
            PdfWriter.getInstance(doc, archivo);
            doc.open();
            Image img = Image.getInstance(getClass().getResource("/Img/logo.png"));
            //Fecha
            String informacion = "SELECT p.*, s.nombre FROM pedidos p INNER JOIN salas s ON p.id_sala = s.id WHERE p.id = ?";
            try {
                ps = con.prepareStatement(informacion);
                ps.setInt(1, id_pedido);
                rs = ps.executeQuery();
                if (rs.next()) {
                    num_mesa = rs.getString("num_mesa");
                    sala = rs.getString("nombre");
                    fechaPedido = rs.getString("fecha");
                    usuario = rs.getString("usuario");
                    total = rs.getString("total");
                }

            } catch (SQLException e) {
                System.out.println(e.toString());
            }
            
            PdfPTable Encabezado = new PdfPTable(4);
            Encabezado.setWidthPercentage(100);
            Encabezado.getDefaultCell().setBorder(0);
            float[] columnWidthsEncabezado = new float[]{20f, 20f, 60f, 60f};
            Encabezado.setWidths(columnWidthsEncabezado);
            Encabezado.setHorizontalAlignment(Element.ALIGN_LEFT);
            Encabezado.addCell(img);
            Encabezado.addCell("");
            //info empresa
            String config = "SELECT * FROM config";
            String mensaje = "";
            try {
                con = cn.getConnection();
                ps = con.prepareStatement(config);
                rs = ps.executeQuery();
                if (rs.next()) {
                    ;
                }
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
            //
            Paragraph info = new Paragraph();
            Font negrita = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.BOLD, BaseColor.BLUE);
            info.add("\nFecha: " + fechaPedido
                    + "\nSala: " + sala
                    + "\nN° Mesa: " + num_mesa
            );
            Encabezado.addCell(info);
            
            doc.add(Encabezado);
            doc.add(Chunk.NEWLINE);
            // Crear la tabla
PdfPTable tabla = new PdfPTable(4);
tabla.setWidthPercentage(100);
tabla.getDefaultCell().setBorder(0);
float[] columnWidths = new float[]{10f, 50f, 15f, 15f};
tabla.setWidths(columnWidths);
tabla.setHorizontalAlignment(Element.ALIGN_LEFT);

// Encabezados de la tabla
PdfPCell c1 = new PdfPCell(new Phrase("Cant.", new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.BOLD)));
PdfPCell c2 = new PdfPCell(new Phrase("Plato.", new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.BOLD)));
PdfPCell c3 = new PdfPCell(new Phrase("P. unt.", new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.BOLD)));
PdfPCell c4 = new PdfPCell(new Phrase("P. Total", new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.BOLD)));
c1.setBorder(Rectangle.NO_BORDER);
c2.setBorder(Rectangle.NO_BORDER);
c3.setBorder(Rectangle.NO_BORDER);
c4.setBorder(Rectangle.NO_BORDER);
c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
c2.setBackgroundColor(BaseColor.LIGHT_GRAY);
c3.setBackgroundColor(BaseColor.LIGHT_GRAY);
c4.setBackgroundColor(BaseColor.LIGHT_GRAY);
tabla.addCell(c1);
tabla.addCell(c2);
tabla.addCell(c3);
tabla.addCell(c4);

// Datos de los platos (con fuente más pequeña)
String product = "SELECT d.* FROM pedidos p INNER JOIN detalle_pedidos d ON p.id = d.id_pedido WHERE p.id = ?";
try {
    ps = con.prepareStatement(product);
    ps.setInt(1, id_pedido);
    rs = ps.executeQuery();
    while (rs.next()) {
        double subTotal = rs.getInt("cantidad") * rs.getDouble("precio");
        
        // Aquí se ajusta la fuente para los datos de los platos
        PdfPCell cantCell = new PdfPCell(new Phrase(rs.getString("cantidad"), new Font(Font.FontFamily.TIMES_ROMAN, 6)));
        PdfPCell platoCell = new PdfPCell(new Phrase(rs.getString("nombre"), new Font(Font.FontFamily.TIMES_ROMAN, 6)));
        PdfPCell precioCell = new PdfPCell(new Phrase(rs.getString("precio"), new Font(Font.FontFamily.TIMES_ROMAN, 6)));
        PdfPCell totalCell = new PdfPCell(new Phrase(String.valueOf(subTotal), new Font(Font.FontFamily.TIMES_ROMAN, 6)));
        
        cantCell.setBorder(Rectangle.NO_BORDER);
        platoCell.setBorder(Rectangle.NO_BORDER);
        precioCell.setBorder(Rectangle.NO_BORDER);
        totalCell.setBorder(Rectangle.NO_BORDER);

        // Añadir celdas a la tabla
        tabla.addCell(cantCell);
        tabla.addCell(platoCell);
        tabla.addCell(precioCell);
        tabla.addCell(totalCell);
    }

} catch (SQLException e) {
    System.out.println(e.toString());
}
            doc.add(tabla);
            Paragraph agra = new Paragraph();
            agra.add(Chunk.NEWLINE);
            agra.add("Total S/: " + total);
            agra.setAlignment(Element.ALIGN_RIGHT);
            doc.add(agra);
            Paragraph firma = new Paragraph();
            firma.setAlignment(Element.ALIGN_CENTER);
            doc.add(firma);
            Paragraph gr = new Paragraph();
            gr.add(Chunk.NEWLINE);
            gr.add(mensaje);
            gr.setAlignment(Element.ALIGN_CENTER);
            doc.add(gr);
            doc.close();
            archivo.close();
            Desktop.getDesktop().open(salida);
        } catch (DocumentException | IOException e) {
            System.out.println(e.toString());
        }finally{
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
        }
    }
    
    public boolean actualizarEstado (int id_pedido){
        String sql = "UPDATE pedidos SET estado = ? WHERE id = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, "FINALIZADO");
            ps.setInt(2, id_pedido);
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        }
    }
    
    public List listarPedidos(){
       List<Pedidos> Lista = new ArrayList();
       String sql = "SELECT p.*, s.nombre FROM pedidos p INNER JOIN salas s ON p.id_sala = s.id ORDER BY p.fecha DESC";
       try {
           con = cn.getConnection();
           ps = con.prepareStatement(sql);
           rs = ps.executeQuery();
           while (rs.next()) {               
               Pedidos ped = new Pedidos();
               ped.setId(rs.getInt("id"));
               ped.setSala(rs.getString("nombre"));
               ped.setNum_mesa(rs.getInt("num_mesa"));
               ped.setFecha(rs.getString("fecha"));
               ped.setTotal(rs.getDouble("total"));
               ped.setUsuario(rs.getString("usuario"));
               ped.setEstado(rs.getString("estado"));
               Lista.add(ped);
           }
       } catch (SQLException e) {
           System.out.println(e.toString());
       }
       return Lista;
   }
    
}
