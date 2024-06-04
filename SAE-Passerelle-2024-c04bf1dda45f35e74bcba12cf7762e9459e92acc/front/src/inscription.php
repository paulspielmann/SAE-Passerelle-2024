<?php
// Connexion à la base de données
$servername = "localhost";
$username = "root"; // Remplacez par votre nom d'utilisateur de la base de données
$password = ""; // Remplacez par votre mot de passe de la base de données
$dbname = "nom_de_votre_base_de_donnees"; // Remplacez par le nom de votre base de données

$conn = new mysqli($servername, $username, $password, $dbname);

// Vérifier la connexion
if ($conn->connect_error) {
    die("Échec de la connexion : " . $conn->connect_error);
}

// Obtenir les données envoyées par la requête POST
$data = json_decode(file_get_contents('php://input'), true);

$username = $data['username'];
$email = $data['email'];
$password = $data['password'];

// Vérifier si l'utilisateur existe déjà
$sql = "SELECT id FROM users WHERE username = ? OR email = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ss", $username, $email);
$stmt->execute();
$stmt->store_result();

if ($stmt->num_rows > 0) {
    echo json_encode(['success' => false, 'message' => 'Nom d\'utilisateur ou email déjà utilisé.']);
} else {
    // Hacher le mot de passe
    $hashed_password = password_hash($password, PASSWORD_DEFAULT);

    // Insérer l'utilisateur dans la base de données
    $sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("sss", $username, $email, $hashed_password);

    if ($stmt->execute()) {
        echo json_encode(['success' => true, 'message' => 'Inscription réussie.']);
    } else {
        echo json_encode(['success' => false, 'message' => 'Erreur lors de l\'inscription.']);
    }
}

$stmt->close();
$conn->close();
?>
