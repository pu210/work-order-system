<template>
	<h3>登入</h3>
	<table>
		<tbody>
			<tr>
				<td>帳號 : </td>
				<td><input type="text" v-model="username"></td>
				<td></td>
			</tr>
			<tr>
				<td>密碼 : </td>
				<td><input type="text" v-model="password"></td>
				<td></td>
			</tr>
			<tr>
				<td> </td>
				<td align="right">
					<button type="button" @click="login">登入</button>
				</td>
			</tr>
		</tbody>
	</table>
</template>
    
<script setup>
	import Swal from 'sweetalert2';
	import { ref } from 'vue';
	import axiosapi from "@/plugins/axios.js"
	

</script>
    
<style>
    
</style>