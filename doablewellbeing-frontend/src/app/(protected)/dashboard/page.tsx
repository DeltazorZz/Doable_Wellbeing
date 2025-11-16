'use client'
import Dashboard from '@/app/components/dashboard/Dashboard'
import Navbar from '@/app/components/navbar'
import React from 'react'

export default function DashboardPage() {
  return (
    <div>  
      <Navbar />
      
      <Dashboard/>
    </div>
  )
}