# Tensor Module Purpose

This module exists to provide a clear and approachable interface for working with indexed tensors in simulation code.

## Design Goal

The goal is readability and usability for physics expressions, not high-speed, low-level linear algebra throughput.  
In particular, this module is **not** intended to replace efficient GPU BLAS libraries or other highly optimized tensor kernels.

Instead, it should make it straightforward to express relationships in index notation and manipulate tensor objects directly in Java code.

## Long-Term Direction

The intended direction is to support writing equations in a form that mirrors physics notation, e.g.:

`Guv = kTuv`

for Einstein-style equations used by the simulation layer.

## Shape Assumption

All tensors in this module are assumed to be square/cubical across indices:

- each index dimension uses the same extent `dim`
- tensor storage size follows `dim ^ rank`

Non-cubical mixed-dimension tensors are intentionally out of scope for this implementation.
