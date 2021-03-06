/*	Copyright 2011 Alexander Bunkenburg alex@inspiracio.com
 * 
 * This file is part of Complex Calculator for Android.
 * 
 * Complex Calculator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Complex Calculator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Complex Calculator for Android. If not, see <http://www.gnu.org/licenses/>.
 * */
package cat.inspiracio.calculator;

import static cat.inspiracio.calculator.Polygon.Direction.EAST;
import static cat.inspiracio.calculator.Polygon.Direction.NORTH;
import static cat.inspiracio.calculator.Polygon.Direction.SOUTH;
import static cat.inspiracio.calculator.Polygon.Direction.WEST;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cat.inspiracio.numbers.Circle;
import cat.inspiracio.numbers.EC;
import cat.inspiracio.numbers.ECList;
import cat.inspiracio.numbers.Line;
import cat.inspiracio.numbers.Piclet;
import cat.inspiracio.numbers.Rectangle;
import cat.inspiracio.view.DragEvent;
import cat.inspiracio.view.MouseEvent;
import cat.inspiracio.view.TouchAdapter;
import cat.inspiracio.view.TouchDispatcher;
import cat.inspiracio.view.ZoomEvent;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.AttributeSet;

/** The complex plane */
final class Plane extends WorldRepresentation{

	/** Pixel distance between the axis tips and the border of the view. */
    private static int AXISSPACE=30;//30
    
    /** Approximate pixel distance between the marks on the axes. */
    private static int AXISMARKING=60;//40
    
    /** Approximate height of the font, to draw axis labels slightly below the x-axis. */
    private static int FONTHEIGHT=18;//10
    
    /** Size of the triangles at the end of the axes. */
    private static int TRIANGLESIZE=10;//5
    
    /** Length of the we marks on the axes. */
    private static int MARKLENGTH=4;//2
    
    /** Initial scale factor */
    private static final double SCALEFACTOR_INITIAL=80;//40

    //State -------------------------------------------------
    
    /** The mathematical distance 1 is how many pixels? */
    private double scaleFactor=SCALEFACTOR_INITIAL;//40D;
    
    private double centerReal;
    private double centerImaginary;
    private double topImaginary;
    private double leftReal;
    private double bottomImaginary;
    private double rightReal;

    //The limits of what is shown, in numbers. (Variables copied from World.)
    //If we have no limits yet, NaN.
    private double maxImaginary=Double.NaN;
    private double minImaginary=Double.NaN;
    private double maxReal=Double.NaN;
    private double minReal=Double.NaN;

    /** The numbers that are currently displayed. */
    private Set<EC>numbers=new HashSet<EC>();
    
    //Constructors ------------------------------------------
	
	/** Constructor for inflation */
	public Plane(Context ctx, AttributeSet ats){
		super(ctx, ats);
		this.setBackgroundColor(Color.WHITE);//this also here, so that it applies also in XML editing in Eclipse
		
		//Touch events
		TouchDispatcher dispatcher=new TouchDispatcher();
		dispatcher.addTouchListener(new TouchAdapter(){
			
			/** Users selects a number by clicking on it. */
			@Override public void onClick(MouseEvent e){
				Point point=e.getPoint();
				EC c=point2Complex(point);
				add(c);				//Show the point on the plane.
				calculator.add(c);	//Also need to send it to the display
			}
			
			/** In calculator-mode, dragging is moving the plane. */
			@Override public void onDrag(DragEvent e){
				Point start=e.getStart();
				Point end=e.getEnd();
				int deltaX=start.x-end.x;
				int deltaY=start.y-end.y;
				Plane.this.shift(deltaX, deltaY);
			}

			/** Zooming. */
			@Override public void onZoom(ZoomEvent e){

				//Keep this point fixed.
				Point fixPoint=e.getCentre();
				EC fixNumber=Plane.this.point2Complex(fixPoint);

				//Math distance from fix to centre, before zooming.
				double deltaX=centerReal-fixNumber.re();
				double deltaY=centerImaginary-fixNumber.im();
				
				//Pixel distance from fix to centre. Keep this constant.
				int pixelX=math2Pix(deltaX);
				int pixelY=math2Pix(deltaY);

				//Now scale.
		    	scaleFactor *= e.getFactor();
		    	
		    	//New math distance from fix to centre, after zooming.
		    	deltaX=pix2Math(pixelX);
		    	deltaY=pix2Math(pixelY);
		    	centerReal=fixNumber.re()+deltaX;
		    	centerImaginary=fixNumber.im()+deltaY;
		    	
		    	invalidate();
			}

		});
		this.setOnTouchListener(dispatcher);
	}
	
	//View methods ------------------------------------------------
	
	/** Draw the world. */
	@Override protected final void onDraw(Canvas canvas){
		//Get ready
		this.setBackgroundColor(Color.WHITE);
		int height=this.getHeight();
		int width=this.getWidth();
		
		//Create paintbrush
		Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLUE);
		//Text size: the default 12 is quite small. The display has 27 by default. Polishing needed.
		paint.setTextSize(FONTHEIGHT);
        Drawing drawing=new Drawing(canvas, paint);
		
        //Some points please
		Point point=new Point();
        Point point1=new Point();
        Point point2=new Point();
        
        //Find the limits of what's visible
        topImaginary = centerImaginary + pix2Math(height / 2);
        bottomImaginary = centerImaginary - pix2Math(height / 2);
        leftReal = centerReal - pix2Math(width / 2);
        rightReal = centerReal + pix2Math(width / 2);
                
        //Draws the x axis.
        double d = raiseSmooth(pix2Math(AXISMARKING));
        int l = math2Pix(d);
        double d1 = 0.0D;
        double d2 = 0.0D;
        double d3 = leftReal + pix2Math(AXISSPACE);
        double d4 = rightReal - pix2Math(AXISSPACE);
        double d5 = bottomImaginary + pix2Math(AXISSPACE);
        double d6 = topImaginary - pix2Math(AXISSPACE);
        if(d3 <= 0.0D && d4 >= 0.0D)
            d1 = 0.0D;
        else if(d3 > 0.0D)
            d1 = d3;
        else if(d4 < 0.0D)
            d1 = d4;
        if(d5 <= 0.0D && d6 >= 0.0D)
            d2 = 0.0D;
        else if(d5 > 0.0D)
            d2 = d5;
        else if(d6 < 0.0D)
            d2 = d6;
        cartesian2Point(d1, d2, point2);
        cartesian2Point(d3, d2, point);
        cartesian2Point(d4, d2, point1);
        drawing.drawLine(point, point1, Color.LTGRAY);
        Polygon polygon = Polygon.mkTriangle(point1, EAST, TRIANGLESIZE);
        drawing.draw(polygon);
        if(!Double.isNaN(maxReal) && rightReal<=maxReal)
            drawing.fill(polygon, Color.RED);
        polygon = Polygon.mkTriangle(point, WEST, TRIANGLESIZE);
        drawing.draw(polygon);
        if(!Double.isNaN(minReal) && minReal<=leftReal)
            drawing.fill(polygon, Color.RED);
        int j = point2.y;
        double d7 = Math.ceil(d3 / d);
        d3 = d7 * d;
        int i = real2Pix(d3);
        for(; d3 < d4; d3 += d){
            drawing.moveTo(i, j);
            drawing.line(0, MARKLENGTH);
            canvas.drawText(EC.toString(d3), i + MARKLENGTH, j + FONTHEIGHT, paint);
            i += l;
        }
        
        //Draws the y axis
        cartesian2Point(d1, d5, point);
        cartesian2Point(d1, d6, point1);
        drawing.drawLine(point, point1, Color.LTGRAY);
        polygon = Polygon.mkTriangle(point1, NORTH, TRIANGLESIZE);
        drawing.draw(polygon);
        if(!Double.isNaN(maxImaginary) && topImaginary <= maxImaginary)
            drawing.fill(polygon, Color.RED);
        polygon = Polygon.mkTriangle(point, SOUTH, TRIANGLESIZE);
        drawing.draw(polygon);
        if(!Double.isNaN(minImaginary) && minImaginary <= bottomImaginary)
            drawing.fill(polygon, Color.RED);
        i = point2.x;
        d7 = Math.ceil(d5 / d);
        d5 = d7 * d;
        for(int k = imag2Pix(d5); d5 < d6; k -= l){
            if(d5 != 0.0D || d1 != 0.0D){
                String s = EC.toString(d5) + "i";
                drawing.moveTo(i, k);
                drawing.line(-MARKLENGTH, 0);
                canvas.drawText(s, i - MARKLENGTH - paint.measureText(s), k + FONTHEIGHT, paint);
            }
            d5 += d;
        }
        
        //Draws the numbers we are currently showing
        this.drawStuff(drawing);
	}

	@Override public final void parcel(String prefix, Bundle b){
		b.putDouble(prefix + ".bottomImaginary", bottomImaginary);
		b.putDouble(prefix + ".centerImaginary", centerImaginary);
		b.putDouble(prefix + ".centerReal", centerReal);
		b.putDouble(prefix + ".leftReal", leftReal);
		b.putDouble(prefix + ".maxImaginary", maxImaginary);
		b.putDouble(prefix + ".maxReal", maxReal);
		b.putDouble(prefix + ".minImaginary", minImaginary);
		b.putDouble(prefix + ".minReal", minReal);
		//numbers
		ArrayList<EC> zs=new ArrayList<EC>(numbers.size());
		zs.addAll(numbers);
		b.putParcelableArrayList(prefix + ".numbers", zs);
		b.putDouble(prefix + ".rightReal", rightReal);
		b.putDouble(prefix + ".scaleFactor", scaleFactor);
		b.putDouble(prefix + ".topImaginary", topImaginary);
	}
		
	@Override public final void unparcel(String prefix, Bundle b){
		bottomImaginary=b.getDouble(prefix + ".bottomImaginary");
		centerImaginary=b.getDouble(prefix + ".centerImaginary");
		centerReal=b.getDouble(prefix + ".centerReal");
		leftReal=b.getDouble(prefix + ".leftReal");
		maxImaginary=b.getDouble(prefix + ".maxImaginary");
		maxReal=b.getDouble(prefix + ".maxReal");
		minImaginary=b.getDouble(prefix + ".minImaginary");
		minReal=b.getDouble(prefix + ".minReal");
		//numbers
		ArrayList<EC>zs=b.getParcelableArrayList(prefix + ".numbers");
		numbers.addAll(zs);
		rightReal=b.getDouble(prefix + ".rightReal");
		scaleFactor=b.getDouble(prefix + ".scaleFactor");
		topImaginary=b.getDouble(prefix + ".topImaginary");
	}
		
	/** Called when the size of the view has changed. */
	@Override public final void onSizeChanged(int a, int b, int c, int d){
		System.out.println("onSizeChanged " + a + " " + b + " " + c + " " + d);
	}
		
	//Business methods ----------------------------------------------

	/** Adds a number to be displayed in the world. */
	@Override final void add(EC c){
		this.updateExtremes(c);
		this.numbers.add(c);
		this.invalidate();
	}

	/** Clears all displayed numbers and stuff. */
	@Override final void clear(){
		this.numbers.clear();
		this.minReal=Double.NaN;
		this.maxReal=Double.NaN;
		this.minImaginary=Double.NaN;
		this.maxImaginary=Double.NaN;
		this.invalidate();
	}
	

	/** Resets to centre on zero. */
    @Override final void reset(){
        centerReal=0.0D;
        centerImaginary=0.0D;
        this.scaleFactor=SCALEFACTOR_INITIAL;
		this.invalidate();
    }

    /** Shift the image by some pixel distance. 
     * @param i horizontal right pixel shift
     * @param j vertical up?/down? pixel shift
     * */
    private void shift(int i, int j){
        centerImaginary -= pix2Math(j);
        centerReal += pix2Math(i);
		this.invalidate();
    }

	//Converters ----------------------------------------------------
	
	/** Converts a complex number in Cartesian coordinates to a point on the plane. 
	 * @param x real part
	 * @param y imaginary part
	 * @param point Sets the properties of this point. */
    private void cartesian2Point(double x, double y, Point point){
        point.x = (int)((x - leftReal) * scaleFactor);
        point.y = -(int)((y - topImaginary) * scaleFactor);
    }

    /** Converts a point to a number. */
	private EC point2Complex(Point point){
		double re=leftReal + (double)point.x / scaleFactor;
		double im=topImaginary - (double)point.y / scaleFactor;
        return EC.mkCartesian(re, im);
    }

    /** Converts pixel distance in mathematical distance between numbers. */
    private double pix2Math(int i){
        return (double)i / scaleFactor;
    }

    /** Converts mathematical distance between numbers in pixel distance. */
    private int math2Pix(double d){
        return (int)(d * scaleFactor);
    }

    /** Real to pixel x-value. */
    private int real2Pix(double d){
        return (int)((d - leftReal) * scaleFactor);
    }

    /** Imaginary to pixel y-value. */
    private int imag2Pix(double d){
        return (int)((topImaginary - d) * scaleFactor);
    }

    /** Works out axis markers with smooth numbers. 
     * Raises d just a little bit to be a multiple of 10, of 2.5, 2, or 1. */
    private static double raiseSmooth(double d){
        int i;
        for(i = 0; d < 1.0D; i--)
            d *= 10D;
        while(d >= 10D){
            d /= 10D;
            i++;
        }
        if(d > 5D)
            d = 10D;
        else if(d > 2.5D)
            d = 5D;
        else if(d > 2D)
            d = 2.5D;
        else if(d > 1.0D)
            d = 2D;
        for(; i < 0; i++)
            d /= 10D;
        for(; i > 0; i--)
            d *= 10D;
        return d;
    }

    //Drawing methods ------------------------------------------------
    
    /** Draws a complex number. */
	private void drawComplex(Drawing drawing, EC ec){
        if(ec.isFinite()){
        	int x=this.real2Pix(ec.re());
        	int y=this.imag2Pix(ec.im());
            drawing.cross(x, y, MARKLENGTH);
            drawing.move(2, 2);//Offset the string a little bit. Should depend on font size.
            drawing.draw(ec.toString());
        }
    }

    /** Draws a list of numbers. */
    private void drawECList(Drawing drawing, ECList eclist){
        if(eclist != null){
            moveTo(drawing, eclist.head());
            lineTo(drawing, eclist.head());
            for(eclist = eclist.tail(); eclist != null; eclist = eclist.tail())
                lineTo(drawing, eclist.head());
        }
    }

    /** Draws a piclet. */
    @SuppressWarnings("unused")
	private void drawPiclet(Drawing drawing, Piclet piclet){
    	//XXX Use subclassing better
        if(piclet instanceof Line){
            moveTo(drawing, ((Line)piclet).start);
            lineTo(drawing, ((Line)piclet).end);
            return;
        }
        if(piclet instanceof Circle){
            Circle circle = (Circle)piclet;
            int x=this.real2Pix(circle.center.re());
            int y=this.imag2Pix(circle.center.im());
            drawing.drawCircle(x, y, math2Pix(circle.radius));
            return;
        }
        if(piclet instanceof Rectangle){
            Rectangle rectangle = (Rectangle)piclet;
            moveTo(drawing, rectangle.botLeft);
            lineTo(drawing, rectangle.botRight);
            lineTo(drawing, rectangle.topRight);
            lineTo(drawing, rectangle.topLeft);
            lineTo(drawing, rectangle.botLeft);
            return;
        } else{
            drawECList(drawing, piclet.getSamples());
            return;
        }
    }

    /** Draws the stuff that the plane should show: just the current numbers. */
    @Override void drawStuff(Drawing drawing){
        for(EC c : this.numbers)
            this.drawComplex(drawing, c);
    }

    /** Draws a line to a number. */
    private void lineTo(Drawing drawing, EC ec){
    	int x=this.real2Pix(ec.re());
    	int y=this.imag2Pix(ec.im());
        drawing.lineTo(x, y);
    }

    /** Moves to a number. */
    private void moveTo(Drawing drawing, EC ec){
    	int x=this.real2Pix(ec.re());
    	int y=this.imag2Pix(ec.im());
        drawing.moveTo(x, y);
    }

    //Helpers that maintain the state ---------------------------------------------------
    
    /** Update the extremes so that they include another number. */
    protected void updateExtremes(EC ec){
        if(ec.isFinite()){
            maxImaginary=max(maxImaginary, ec.im());
            minImaginary=min(minImaginary, ec.im());
            maxReal=max(maxReal, ec.re());
            minReal=min(minReal, ec.re());
        }
    }

    /** Like Math.max(double, double), but if one is NaN, returns the other. */
    private double max(double d, double e){
    	if(Double.isNaN(d))
    		return e;
    	if(Double.isNaN(e))
    		return d;
    	return Math.max(d, e);
    }

    /** Like Math.min(double, double), but if one is NaN, returns the other. */
    private double min(double d, double e){
    	if(Double.isNaN(d))
    		return e;
    	if(Double.isNaN(e))
    		return d;
    	return Math.min(d, e);
    }
}