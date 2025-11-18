% entrenamientos.pl

:- dynamic requiere/2.
:- dynamic tiene_base/2.

% base_count(Rol, CantBase)
%  - Si hay un hecho tiene_base(Rol, X), usamos ese X.
%  - Si no hay ninguno, asumimos 0.
base_count(Rol, CantBase) :-
    (   tiene_base(Rol, CantBase)
    ->  true
    ;   CantBase = 0
    ).

% entrenamientos_rol(Rol, Entrenamientos)
% CantMaxima: máxima cantidad simultánea requerida para Rol
% CantBase  : cuántos artistas base cubren Rol
% Entrenamientos = max(0, CantMaxima - CantBase)

entrenamientos_rol(Rol, Entrenamientos) :-
    requiere(Rol, CantMaxima),
    base_count(Rol, CantBase),
    Falta is CantMaxima - CantBase,
    Falta > 0,
    Entrenamientos is Falta.

% Si no falta nadie para ese rol → 0 entrenamientos
entrenamientos_rol(Rol, 0) :-
    requiere(Rol, CantMaxima),
    base_count(Rol, CantBase),
    CantMaxima =< CantBase.

% min_entrenamientos(N):
%   suma de los entrenamientos necesarios para todos los roles requeridos
min_entrenamientos(N) :-
    setof(Rol, Cant^requiere(Rol, Cant), Roles),
    findall(E, (member(R, Roles), entrenamientos_rol(R, E)), Entrs),
    sum_list(Entrs, N).

% costo_total_entrenamiento(CostoUnitario, CostoTotal)
costo_total_entrenamiento(CostoUnitario, CostoTotal) :-
    min_entrenamientos(N),
    CostoTotal is N * CostoUnitario.