 /*
  *  Copyright 2006-2007 Columbia University.
  *
  *  This file is part of MEAPsoft.
  *
  *  MEAPsoft is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License version 2 as
  *  published by the Free Software Foundation.
  *
  *  MEAPsoft is distributed in the hope that it will be useful, but
  *  WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with MEAPsoft; if not, write to the Free Software
  *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  *  02110-1301 USA
  *
  *  See the file "COPYING" for the text of the license.
  */
 
 package com.limbrescue.limbrescueangularappbackend.controller;
 
public class FFT 
{
    public static class Complex 
    {
        private final double re; // the real part
        private final double im; // the imaginary part
 
        // create a new object with the given real and imaginary parts
        public Complex(double real, double imag) 
        {
            re = real;
            im = imag;
        }
 
        // return a string representation of the invoking Complex object
        public String toString() 
        {
            if (im == 0)
                return re + "";
            if (re == 0)
                return im + "i";
            if (im < 0)
                return re + " - " + (-im) + "i";
            return re + " + " + im + "i";
        }
 
        // return abs/modulus/magnitude and angle/phase/argument
        public double abs() 
        {
            return Math.hypot(re, im);
        } // Math.sqrt(re*re + im*im)
 
        public double phase() 
        {
            return Math.atan2(im, re);
        } // between -pi and pi
 
        // return a new Complex object whose value is (this + b)
        public Complex plus(Complex b) 
        {
            Complex a = this; // invoking object
            double real = a.re + b.re;
            double imag = a.im + b.im;
            return new Complex(real, imag);
        }
 
        // return a new Complex object whose value is (this - b)
        public Complex minus(Complex b) 
        {
            Complex a = this;
            double real = a.re - b.re;
            double imag = a.im - b.im;
            return new Complex(real, imag);
        }
 
        // return a new Complex object whose value is (this * b)
        public Complex times(Complex b) 
        {
            Complex a = this;
            double real = a.re * b.re - a.im * b.im;
            double imag = a.re * b.im + a.im * b.re;
            return new Complex(real, imag);
        }
 
        // scalar multiplication
        // return a new object whose value is (this * alpha)
        public Complex times(double alpha) 
        {
            return new Complex(alpha * re, alpha * im);
        }
 
        // return a new Complex object whose value is the conjugate of this
        public Complex conjugate() {
            return new Complex(re, -im);
        }
 
        // return a new Complex object whose value is the reciprocal of this
        public Complex reciprocal() 
        {
            double scale = re * re + im * im;
            return new Complex(re / scale, -im / scale);
        }
 
        // return the real or imaginary part
        public double re() 
        {
            return re;
        }
 
        public double im() 
        {
            return im;
        }
 
        // return a / b
        public Complex divides(Complex b) 
        {
            Complex a = this;
            return a.times(b.reciprocal());
        }
 
        // return a new Complex object whose value is the complex exponential of
        // this
        public Complex exp() 
        {
            return new Complex(Math.exp(re) * Math.cos(im), Math.exp(re)
                    * Math.sin(im));
        }
 
        // return a new Complex object whose value is the complex sine of this
        public Complex sin() 
        {
            return new Complex(Math.sin(re) * Math.cosh(im), Math.cos(re)
                    * Math.sinh(im));
        }
 
        // return a new Complex object whose value is the complex cosine of this
        public Complex cos() 
        {
            return new Complex(Math.cos(re) * Math.cosh(im), -Math.sin(re)
                    * Math.sinh(im));
        }
 
        // return a new Complex object whose value is the complex tangent of
        // this
        public Complex tan() 
        {
            return sin().divides(cos());
        }
 
        // a static version of plus
        public static Complex plus(Complex a, Complex b) 
        {
            double real = a.re + b.re;
            double imag = a.im + b.im;
            Complex sum = new Complex(real, imag);
            return sum;
        }
 
        // compute the FFT of x[], assuming its length is a power of 2
        public static Complex[] fft(Complex[] x) 
        {
            int N = x.length;
 
            // base case
            if (N == 1)
                return new Complex[] { x[0] };
 
            // radix 2 Cooley-Tukey FFT
            if (N % 2 != 0) 
            {
                throw new RuntimeException("N is not a power of 2");
            }
 
            // fft of even terms
            Complex[] even = new Complex[N / 2];
            for (int k = 0; k < N / 2; k++) 
            {
                even[k] = x[2 * k];
            }
            Complex[] q = fft(even);
 
            // fft of odd terms
            Complex[] odd = even; // reuse the array
            for (int k = 0; k < N / 2; k++) 
            {
                odd[k] = x[2 * k + 1];
            }
            Complex[] r = fft(odd);
 
            // combine
            Complex[] y = new Complex[N];
            for (int k = 0; k < N / 2; k++) 
            {
                double kth = -2 * k * Math.PI / N;
                Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
                y[k] = q[k].plus(wk.times(r[k]));
                y[k + N / 2] = q[k].minus(wk.times(r[k]));
            }
            return y;
        }
 
        // compute the inverse FFT of x[], assuming its length is a power of 2
        public static Complex[] ifft(Complex[] x) 
        {
            int N = x.length;
            Complex[] y = new Complex[N];
 
            // take conjugate
            for (int i = 0; i < N; i++) 
            {
                y[i] = x[i].conjugate();
            }
 
            // compute forward FFT
            y = fft(y);
 
            // take conjugate again
            for (int i = 0; i < N; i++) 
            {
                y[i] = y[i].conjugate();
            }
 
            // divide by N
            for (int i = 0; i < N; i++) 
            {
                y[i] = y[i].times(1.0 / N);
            }
 
            return y;
 
        }
 
        // display an array of Complex numbers to standard output
        public static void show(Complex[] x, String title) 
        {
            System.out.println(title);
            for (int i = 0; i < x.length; i++) 
            {
                System.out.println(x[i]);
            }
            System.out.println();
        }
 
        public static void main(String[] args) 
        {
            int N = 8;//Integer.parseInt(args[0]);
            Complex[] x = new Complex[N];
 
            // original data
            for (int i = 0; i < N; i++) 
            {
                x[i] = new Complex(i, 0);
                x[i] = new Complex(-2 * Math.random() + 1, 0);
            }
            show(x, "x");
 
            // FFT of original data
            Complex[] y = fft(x);
            show(y, "y = fft(x)");
 
            // take inverse FFT
            Complex[] z = ifft(y);
            show(z, "z = ifft(y)");
 
        }
 
    }
}
 
 

