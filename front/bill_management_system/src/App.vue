<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  height: 100vh !important;
  display: flex !important;
  justify-content: center !important;
  align-items: center !important;
  margin: 0 !important;
  padding: 0 !important;
  max-width: none !important;
}
</style>
