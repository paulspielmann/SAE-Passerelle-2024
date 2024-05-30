CREATE TABLE Utilisateurs (
    ID_utilisateur SERIAL PRIMARY KEY,
    Mdp VARCHAR(100),
    Email VARCHAR(100)
);

CREATE TABLE Parties (
    ID_partie SERIAL PRIMARY KEY,
    ID_utilisateur_blanc INTEGER REFERENCES Utilisateurs(ID_utilisateur),
    ID_utilisateur_noir INTEGER REFERENCES Utilisateurs(ID_utilisateur),
    Date DATE,
    Resultat VARCHAR(50),
    Coups TEXT -- ou JSON pour stocker les coups
);
