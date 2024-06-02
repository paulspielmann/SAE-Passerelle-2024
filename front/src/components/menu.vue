<template>
    <nav class="menu">
      <ul>
        <li><router-link to="/">Menu Principal</router-link></li>
        <li><router-link to="/profil">Profil</router-link></li>
      </ul>
    </nav>
  </template>
  
  <script>
  export default {
    name: 'Menu'
  };
  </script>
  
  <style scoped>
  .menu {
    background-color: #333;
    color: white;
    padding: 10px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  .menu ul {
    list-style-type: none;
    padding: 0;
    margin: 0;
    display: flex;
  }
  
  .menu li {
    margin-right: 20px;
  }
  
  .menu a {
    color: white;
    text-decoration: none;
    font-weight: bold;
  }
  
  .menu a:hover {
    text-decoration: underline;
  }
  </style>
  