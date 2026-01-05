'use client'
import { DashboardPage } from '@/app/components/dashboard/DashboardPage'
import Navbar from '@/app/components/navbar'
import React from 'react'

export default function Dashboard() {
  return (
    <div>  
      <Navbar />
      <DashboardPage/>      
    </div>
  )
}