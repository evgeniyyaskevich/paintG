import static entity.DrawAction.CHAIN;
import static entity.DrawAction.CHAIN_UPDATE;
import static entity.DrawAction.CIRCLE;
import static entity.DrawAction.ELLIPSE;
import static entity.DrawAction.LINE;
import static entity.DrawAction.MOVEMENT;
import static entity.DrawAction.POLYGON;
import static entity.DrawAction.POLYGON_UPDATE;
import static entity.DrawAction.RAY;
import static entity.DrawAction.RECTANGLE;
import static entity.DrawAction.RHOMBUS;
import static entity.DrawAction.RIGHT_POLYGON;
import static entity.DrawAction.SEGMENT;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import component.IntInputDialog;
import entity.Chain;
import entity.Circle;
import entity.DrawAction;
import entity.Ellipse;
import entity.Line;
import entity.Polygon;
import entity.Ray;
import entity.Rectangle;
import entity.Rhombus;
import entity.RightPolygon;
import entity.Segment;
import entity.Shape;

public class App extends JFrame {

    private JPanel rootPanel, drawPanel;
    private JButton segmentButton, rayButton, lineButton, chainButton, circleButton, ellipseButton, polygonButton,
            rhombusButton, rectangleButton, rightPolygonButton, fillColorButton, borderColorButton, moveButton;
    private IntInputDialog rightPolygonInput;
    private DrawAction drawAction = DrawAction.MOVEMENT;
    private DrawAction previousDrawAction = MOVEMENT;
    private Map<DrawAction, Consumer<MouseEvent>> drawActionImpl;
    private Map<DrawAction, Consumer<MouseEvent>> creationActions;
    private Color borderColor = new Color(0, 0, 0);
    private Color fillColor = new Color(255, 120, 120);
    private List<Shape> shapes = new ArrayList<>();
    private boolean isDragging;

    private App() {

        initComponents();
        initDrawModeActions();
        rightPolygonInput = new IntInputDialog(this);
        setContentPane(rootPanel);
        setSize(1000, 700);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        showMessageDialog(null, "Hello.\n 1. To add new point in polygon or chain just click. "
                + "\n 2. To stop drawing chain or polygon press right mouse button.");

    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(App::new);
    }

    private void initComponents() {

        drawPanel.addMouseListener(getMouseAdapter());
        drawPanel.addMouseMotionListener(getMouseMotionListener());
        segmentButton.addActionListener(e -> drawAction = DrawAction.SEGMENT);
        rayButton.addActionListener(e -> drawAction = DrawAction.RAY);
        lineButton.addActionListener(e -> drawAction = LINE);
        chainButton.addActionListener(e -> drawAction = CHAIN);
        circleButton.addChangeListener(e -> drawAction = CIRCLE);
        ellipseButton.addActionListener(e -> drawAction = ELLIPSE);
        polygonButton.addActionListener(e -> drawAction = POLYGON);
        rhombusButton.addActionListener(e -> drawAction = RHOMBUS);
        rectangleButton.addActionListener(e -> drawAction = RECTANGLE);
        rightPolygonButton.addActionListener(e -> {
            drawAction = RIGHT_POLYGON;
            rightPolygonInput.setVisible(true);
        });
        fillColorButton.addActionListener(e -> fillColor = JColorChooser.showDialog(this,
                "Choose fill color", Color.white));
        borderColorButton.addActionListener(e -> borderColor = JColorChooser.showDialog(this,
                "Choose border color", Color.BLACK));
        moveButton.addActionListener(e -> drawAction = MOVEMENT);
    }

    private void initDrawModeActions() {

        drawActionImpl = new HashMap<>();
        drawActionImpl.put(MOVEMENT, e -> {
            if (isDragging) {
                getCurrentShape().move(e.getPoint());
            }
        });
        drawActionImpl.put(LINE, e -> {
            Line line = (Line) App.this.getCurrentShape();
            line.setEnd(e.getPoint());
        });
        drawActionImpl.put(SEGMENT, e -> {

            Segment segment = (Segment) getCurrentShape();
            segment.setEnd(e.getPoint());
        });
        drawActionImpl.put(RAY, e -> {

            Ray ray = (Ray) getCurrentShape();
            ray.setEnd(e.getPoint());
        });
        drawActionImpl.put(CHAIN, e -> {

            Chain chain = (Chain) getCurrentShape();
            chain.getLastSegment().setEnd(e.getPoint());
            previousDrawAction = CHAIN;
            drawAction = CHAIN_UPDATE;
        });
        drawActionImpl.put(CHAIN_UPDATE, e -> {

            Chain chain = (Chain) getCurrentShape();
            chain.getLastSegment().setEnd(e.getPoint());
        });
        drawActionImpl.put(CIRCLE, e -> {

            Circle circle = (Circle) getCurrentShape();
            circle.setCorner(e.getPoint());
        });
        drawActionImpl.put(ELLIPSE, e -> {

            Ellipse ellipse = (Ellipse) getCurrentShape();
            ellipse.setCorner(e.getPoint());
        });
        drawActionImpl.put(POLYGON, e -> {

            Polygon polygon = (Polygon) getCurrentShape();
            polygon.setLastPoint(e.getPoint());
            previousDrawAction = POLYGON;
            drawAction = POLYGON_UPDATE;
        });
        drawActionImpl.put(POLYGON_UPDATE, e -> {

            Polygon polygon = (Polygon) getCurrentShape();
            polygon.setLastPoint(e.getPoint());
        });
        drawActionImpl.put(RHOMBUS, e -> {
            Rhombus rhombus = (Rhombus) getCurrentShape();
            rhombus.setCorner(e.getPoint());
        });
        drawActionImpl.put(RECTANGLE, e -> {

            Rectangle rectangle = (Rectangle) getCurrentShape();
            rectangle.setCorner(e.getPoint());
        });
        drawActionImpl.put(RIGHT_POLYGON, e -> {

            RightPolygon rightPolygon = (RightPolygon) getCurrentShape();
            rightPolygon.setCorner(e.getPoint());
        });

        creationActions = new HashMap<>();
        creationActions.put(LINE, e -> shapes.add(new Line(borderColor, e.getPoint(), e.getPoint())));
        creationActions.put(SEGMENT, e -> shapes.add(new Segment(borderColor, e.getPoint(), e.getPoint())));
        creationActions.put(RAY, e -> shapes.add(new Ray(borderColor, e.getPoint(), e.getPoint())));
        creationActions.put(CHAIN, e -> {
            List<Segment> segments = new ArrayList<>();
            segments.add(new Segment(borderColor, e.getPoint(), e.getPoint()));
            shapes.add(new Chain(borderColor, e.getPoint(), segments));
        });
        creationActions.put(CHAIN_UPDATE, e -> {
            Chain chain = (Chain) getCurrentShape();
            chain.addSegment(new Segment(borderColor, chain.getLastSegment().getEnd(), e.getPoint()));
        });
        creationActions.put(CIRCLE,
                e -> shapes.add(new Circle(borderColor, e.getPoint(), fillColor, e.getPoint())));
        creationActions.put(ELLIPSE,
                e -> shapes.add(new Ellipse(borderColor, e.getPoint(), fillColor, e.getPoint())));
        creationActions.put(POLYGON, e -> {
            List<Point> points = new ArrayList<>();
            points.add(e.getPoint());
            points.add(e.getPoint());
            shapes.add(new Polygon(borderColor, e.getPoint(), fillColor, points));
        });
        creationActions.put(POLYGON_UPDATE, e -> {

            Polygon polygon = (Polygon) getCurrentShape();
            polygon.addPoint(e.getPoint());
        });
        creationActions.put(RHOMBUS,
                e -> shapes.add(new Rhombus(borderColor, e.getPoint(), e.getPoint(), fillColor)));
        creationActions.put(RECTANGLE,
                e -> shapes.add(new Rectangle(borderColor, e.getPoint(), e.getPoint(), fillColor)));
        creationActions.put(RIGHT_POLYGON,
                e -> shapes.add(new RightPolygon(borderColor, e.getPoint(), e.getPoint(), fillColor,
                        rightPolygonInput.getValue())));
        creationActions.put(MOVEMENT, e -> {
            for (Shape shape : shapes) {
                if (shape.contains(e.getPoint())) {
                    isDragging = true;
                    shapes.remove(shape);
                    shapes.add(shape);
                    break;
                }
            }
        });
    }

    private MouseListener getMouseAdapter() {

        return new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {

                int cursorType = drawAction == DrawAction.MOVEMENT ? Cursor.HAND_CURSOR : Cursor.CROSSHAIR_CURSOR;
                setCursor(new Cursor(cursorType));
            }

            @Override
            public void mouseExited(MouseEvent e) {

                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mousePressed(MouseEvent e) {

                if (SwingUtilities.isRightMouseButton(e)) {
                    drawAction = previousDrawAction;
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    creationActions.get(drawAction).accept(e);
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

                if (drawAction == MOVEMENT) {
                    isDragging = false;
                }
            }
        };
    }

    private MouseMotionListener getMouseMotionListener() {

        return new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {

                if (SwingUtilities.isLeftMouseButton(e)) {
                    drawActionImpl.get(drawAction).accept(e);
                    repaint();
                }
            }
        };
    }

    private void createUIComponents() {

        drawPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {

                super.paintComponent(g);
                shapes.forEach(s -> s.draw((Graphics2D) g));
            }
        };
    }

    private Shape getCurrentShape() {

        return shapes.get(shapes.size() - 1);
    }
}
